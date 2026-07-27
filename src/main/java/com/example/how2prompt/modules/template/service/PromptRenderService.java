package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.template.dto.FieldError;
import com.example.how2prompt.modules.template.dto.RenderResult;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.entity.TemplateVariable;
import com.example.how2prompt.modules.template.entity.TemplateVariant;
import com.example.how2prompt.modules.template.entity.TemplateVersion;
import com.example.how2prompt.modules.template.exception.TemplateValidationException;
import com.example.how2prompt.modules.template.repository.TemplateRepository;
import com.example.how2prompt.modules.template.repository.TemplateVariableRepository;
import com.example.how2prompt.modules.template.repository.TemplateVariantRepository;
import com.example.how2prompt.modules.template.repository.TemplateVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Public API render prompt của module template (Epic 3).
 * <p>
 * <strong>Contract cho module prompt (Dev C):</strong> đây là source of truth phía backend —
 * mọi generate/audit phải gọi {@link #render} để đảm bảo kết quả khớp preview client
 * (cùng quy tắc placeholder + variant + extra instructions).
 *
 * <h2>Quy trình</h2>
 * <ol>
 *   <li>Load template + current {@code template_version}</li>
 *   <li>Validate {@code inputValues} qua {@link TemplateVariableValidator}</li>
 *   <li>Chọn body: nếu có {@link TemplateVariant} khớp {@code aiModelId} và
 *       {@code prompt_body_override} không null → <em>thay thế toàn bộ</em> (không merge)</li>
 *   <li>Render placeholder {@code {{var_key}}}</li>
 *   <li>Nối {@code extraInstructions} vào cuối bằng {@code \n\n} (nếu có)</li>
 * </ol>
 *
 * <h2>Quy tắc placeholder</h2>
 * <ul>
 *   <li>Có giá trị trong input → dùng giá trị</li>
 *   <li>Không có nhưng variable có {@code default_value} → dùng default</li>
 *   <li>Không có cả hai và không bắt buộc → chuỗi rỗng</li>
 *   <li>{@code var_key} trong body không khai báo variable (lỗi soạn template) →
 *       giữ nguyên placeholder gốc + log WARN, <strong>không</strong> throw cho end-user</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptRenderService {

    /** Khớp {@code {{var_key}}} — var_key: chữ, số, underscore. */
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([a-zA-Z0-9_]+)\\}\\}");

    private final TemplateRepository templateRepository;
    private final TemplateVersionRepository templateVersionRepository;
    private final TemplateVariableRepository templateVariableRepository;
    private final TemplateVariantRepository templateVariantRepository;
    private final TemplateVariableValidator templateVariableValidator;

    /**
     * Render prompt cuối cùng cho template + model đích.
     *
     * @param templateId         id template
     * @param aiModelId          model AI user chọn (có thể null → không áp variant)
     * @param inputValues        map var_key → giá trị form
     * @param extraInstructions  free-form instructions (US-3.4); null/blank = bỏ qua
     * @return {@link RenderResult} DTO thuần
     * @throws ResourceNotFoundException   nếu template / current version không tồn tại
     * @throws TemplateValidationException nếu input vi phạm rule variable
     */
    @Transactional(readOnly = true)
    public RenderResult render(
            UUID templateId,
            UUID aiModelId,
            Map<String, Object> inputValues,
            String extraInstructions
    ) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> ResourceNotFoundException.of("Template", templateId));

        TemplateVersion version = resolveCurrentVersion(template);
        List<TemplateVariable> variables =
                templateVariableRepository.findByTemplateVersionIdOrderBySortOrderAsc(version.getId());

        Map<String, Object> safeInputs = inputValues != null ? inputValues : Map.of();

        List<FieldError> errors = templateVariableValidator.validate(variables, safeInputs);
        if (!errors.isEmpty()) {
            throw new TemplateValidationException(errors);
        }

        Map<String, TemplateVariable> variableByKey = new HashMap<>();
        for (TemplateVariable variable : variables) {
            variableByKey.put(variable.getVarKey(), variable);
        }

        Map<String, Object> resolved = resolveValues(variableByKey, safeInputs);

        String promptBody = version.getPromptBody();
        String systemPrompt = version.getSystemPrompt();
        boolean usedVariant = false;

        if (aiModelId != null) {
            Optional<TemplateVariant> variantOpt =
                    templateVariantRepository.findByTemplateVersionIdAndAiModelId(version.getId(), aiModelId);
            if (variantOpt.isPresent()) {
                TemplateVariant variant = variantOpt.get();
                // prompt_body_override thay thế toàn bộ — không merge với body gốc
                if (variant.getPromptBodyOverride() != null) {
                    promptBody = variant.getPromptBodyOverride();
                    usedVariant = true;
                }
                if (variant.getSystemPromptOverride() != null) {
                    systemPrompt = variant.getSystemPromptOverride();
                    usedVariant = true;
                }
            }
        }

        String rendered = renderPlaceholders(promptBody, variableByKey, resolved);
        String normalizedExtra = StringUtils.hasText(extraInstructions) ? extraInstructions.trim() : null;
        if (normalizedExtra != null) {
            rendered = rendered + "\n\n" + normalizedExtra;
        }

        return new RenderResult(
                template.getId(),
                version.getId(),
                aiModelId,
                rendered,
                systemPrompt,
                usedVariant,
                Map.copyOf(resolved),
                normalizedExtra
        );
    }

    private TemplateVersion resolveCurrentVersion(Template template) {
        if (template.getCurrentVersionId() != null) {
            Optional<TemplateVersion> byId = templateVersionRepository.findById(template.getCurrentVersionId());
            if (byId.isPresent()) {
                return byId.get();
            }
        }
        return templateVersionRepository.findByTemplateIdAndCurrentTrue(template.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Template chưa có current version: " + template.getId()));
    }

    /**
     * Resolve giá trị cuối cùng per var_key theo quy tắc placeholder (trước khi substitute).
     */
    private static Map<String, Object> resolveValues(
            Map<String, TemplateVariable> variableByKey,
            Map<String, Object> inputValues
    ) {
        Map<String, Object> resolved = new HashMap<>();
        for (Map.Entry<String, TemplateVariable> entry : variableByKey.entrySet()) {
            String key = entry.getKey();
            TemplateVariable variable = entry.getValue();
            if (inputValues.containsKey(key) && !isBlankValue(inputValues.get(key))) {
                resolved.put(key, inputValues.get(key));
            } else if (StringUtils.hasText(variable.getDefaultValue())) {
                resolved.put(key, variable.getDefaultValue());
            } else {
                resolved.put(key, "");
            }
        }
        return resolved;
    }

    private static String renderPlaceholders(
            String body,
            Map<String, TemplateVariable> variableByKey,
            Map<String, Object> resolved
    ) {
        if (body == null || body.isEmpty()) {
            return "";
        }
        Matcher matcher = PLACEHOLDER.matcher(body);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String varKey = matcher.group(1);
            String replacement;
            if (variableByKey.containsKey(varKey)) {
                replacement = toDisplayString(resolved.get(varKey));
            } else {
                // Lỗi soạn template — giữ nguyên placeholder, không fail end-user
                log.warn("Placeholder var_key '{}' không có trong template_variables — giữ nguyên.", varKey);
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static boolean isBlankValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String s) {
            return !StringUtils.hasText(s);
        }
        if (value instanceof Collection<?> c) {
            return c.isEmpty();
        }
        return false;
    }

    private static String toDisplayString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
        }
        if (value instanceof Object[] array) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                if (array[i] == null) {
                    continue;
                }
                if (!sb.isEmpty()) {
                    sb.append(", ");
                }
                sb.append(array[i]);
            }
            return sb.toString();
        }
        return String.valueOf(value);
    }
}
