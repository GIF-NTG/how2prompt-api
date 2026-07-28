package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.modules.template.dto.FieldError;
import com.example.how2prompt.modules.template.entity.TemplateVariable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Helper nội bộ module template — validate {@code inputValues} theo cấu hình
 * {@link TemplateVariable}. Gom <strong>toàn bộ</strong> lỗi một lần, không throw
 * ở lỗi đầu tiên. Caller (vd. {@link PromptRenderService}) tự quyết định throw.
 */
@Component
class TemplateVariableValidator {

    private static final String CODE_REQUIRED = "REQUIRED";
    private static final String CODE_MIN = "MIN";
    private static final String CODE_MAX = "MAX";
    private static final String CODE_MIN_LENGTH = "MIN_LENGTH";
    private static final String CODE_MAX_LENGTH = "MAX_LENGTH";
    private static final String CODE_REGEX = "REGEX";
    private static final String CODE_INVALID_OPTION = "INVALID_OPTION";
    private static final String CODE_INVALID_TYPE = "INVALID_TYPE";

    /**
     * @param variables   danh sách biến của current template version
     * @param inputValues giá trị user nhập (key = var_key)
     * @return list lỗi (rỗng nếu hợp lệ)
     */
    List<FieldError> validate(List<TemplateVariable> variables, Map<String, Object> inputValues) {
        List<FieldError> errors = new ArrayList<>();
        Map<String, Object> values = inputValues != null ? inputValues : Map.of();

        if (variables == null || variables.isEmpty()) {
            return errors;
        }

        for (TemplateVariable variable : variables) {
            String key = variable.getVarKey();
            Object raw = values.get(key);
            boolean missing = isMissing(raw);

            if (variable.isRequired() && missing) {
                // is_required: bắt buộc có giá trị trong input (default_value chỉ dùng lúc render
                // khi field optional / thiếu giá trị).
                errors.add(new FieldError(key, CODE_REQUIRED, "Trường bắt buộc."));
                continue;
            }

            if (missing) {
                continue;
            }

            String inputType = normalizeType(variable.getInputType());
            Map<String, Object> rules = variable.getValidation() != null
                    ? variable.getValidation()
                    : Map.of();

            switch (inputType) {
                case "number", "slider" -> validateNumber(key, raw, rules, errors);
                case "boolean" -> validateBoolean(key, raw, errors);
                case "select" -> validateSelect(key, raw, variable.getOptions(), errors);
                case "multiselect" -> validateMultiselect(key, raw, variable.getOptions(), errors);
                case "text", "textarea" -> validateText(key, raw, rules, errors);
                default -> validateText(key, raw, rules, errors);
            }
        }

        return errors;
    }

    private static void validateText(String key, Object raw, Map<String, Object> rules, List<FieldError> errors) {
        String text = stringify(raw);
        Integer minLength = asInteger(rules.get("minLength"));
        Integer maxLength = asInteger(rules.get("maxLength"));
        String regex = asString(rules.get("regex"));

        if (minLength != null && text.length() < minLength) {
            errors.add(new FieldError(key, CODE_MIN_LENGTH,
                    "Độ dài tối thiểu là " + minLength + " ký tự."));
        }
        if (maxLength != null && text.length() > maxLength) {
            errors.add(new FieldError(key, CODE_MAX_LENGTH,
                    "Độ dài tối đa là " + maxLength + " ký tự."));
        }
        if (StringUtils.hasText(regex)) {
            try {
                if (!Pattern.compile(regex).matcher(text).matches()) {
                    errors.add(new FieldError(key, CODE_REGEX, "Giá trị không khớp định dạng."));
                }
            } catch (PatternSyntaxException ex) {
                // Cấu hình template sai — không block end-user bằng lỗi regex engine
                errors.add(new FieldError(key, CODE_REGEX, "Cấu hình regex không hợp lệ."));
            }
        }
    }

    private static void validateNumber(String key, Object raw, Map<String, Object> rules, List<FieldError> errors) {
        Double number = asDouble(raw);
        if (number == null) {
            errors.add(new FieldError(key, CODE_INVALID_TYPE, "Giá trị phải là số."));
            return;
        }
        Double min = asDouble(rules.get("min"));
        Double max = asDouble(rules.get("max"));
        if (min != null && number < min) {
            errors.add(new FieldError(key, CODE_MIN, "Giá trị tối thiểu là " + min + "."));
        }
        if (max != null && number > max) {
            errors.add(new FieldError(key, CODE_MAX, "Giá trị tối đa là " + max + "."));
        }
    }

    private static void validateBoolean(String key, Object raw, List<FieldError> errors) {
        if (raw instanceof Boolean) {
            return;
        }
        if (raw instanceof String s) {
            String lower = s.trim().toLowerCase(Locale.ROOT);
            if ("true".equals(lower) || "false".equals(lower) || "1".equals(lower) || "0".equals(lower)) {
                return;
            }
        }
        if (raw instanceof Number n) {
            double value = n.doubleValue();
            if (Double.compare(value, 0D) == 0 || Double.compare(value, 1D) == 0) {
                return;
            }
        }
        errors.add(new FieldError(key, CODE_INVALID_TYPE, "Giá trị phải là boolean."));
    }

    private static void validateSelect(String key, Object raw, List<Object> options, List<FieldError> errors) {
        Set<String> allowed = optionValues(options);
        // options rỗng = cấu hình template chưa xong — không block end-user
        if (allowed.isEmpty()) {
            return;
        }
        String value = stringify(raw);
        if (!allowed.contains(value)) {
            errors.add(new FieldError(key, CODE_INVALID_OPTION, "Giá trị không nằm trong options."));
        }
    }

    private static void validateMultiselect(String key, Object raw, List<Object> options, List<FieldError> errors) {
        List<String> selected = toStringList(raw);
        if (selected == null) {
            errors.add(new FieldError(key, CODE_INVALID_TYPE, "Giá trị multiselect phải là mảng."));
            return;
        }
        Set<String> allowed = optionValues(options);
        if (allowed.isEmpty()) {
            return;
        }
        for (String value : selected) {
            if (!allowed.contains(value)) {
                errors.add(new FieldError(key, CODE_INVALID_OPTION,
                        "Giá trị '" + value + "' không nằm trong options."));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static boolean isMissing(Object raw) {
        if (raw == null) {
            return true;
        }
        if (raw instanceof String s) {
            return !StringUtils.hasText(s);
        }
        if (raw instanceof Collection<?> c) {
            return c.isEmpty();
        }
        if (raw instanceof Map<?, ?> m) {
            return m.isEmpty();
        }
        return false;
    }

    private static String normalizeType(String inputType) {
        return inputType == null ? "text" : inputType.trim().toLowerCase(Locale.ROOT);
    }

    private static Set<String> optionValues(List<Object> options) {
        Set<String> values = new LinkedHashSet<>();
        if (options == null) {
            return values;
        }
        for (Object option : options) {
            if (option == null) {
                continue;
            }
            if (option instanceof Map<?, ?> map) {
                Object value = map.containsKey("value") ? map.get("value") : map.get("id");
                if (value != null) {
                    values.add(String.valueOf(value));
                }
            } else {
                values.add(String.valueOf(option));
            }
        }
        return values;
    }

    private static List<String> toStringList(Object raw) {
        if (raw instanceof Collection<?> collection) {
            List<String> result = new ArrayList<>();
            for (Object item : collection) {
                if (item != null) {
                    result.add(String.valueOf(item));
                }
            }
            return result;
        }
        if (raw instanceof Object[] array) {
            List<String> result = new ArrayList<>();
            for (Object item : array) {
                if (item != null) {
                    result.add(String.valueOf(item));
                }
            }
            return result;
        }
        // single value treated as one-element multiselect
        if (raw instanceof String || raw instanceof Number || raw instanceof Boolean) {
            return List.of(String.valueOf(raw));
        }
        return null;
    }

    private static String stringify(Object raw) {
        if (raw == null) {
            return "";
        }
        if (raw instanceof Collection<?> collection) {
            return collection.stream().filter(Objects::nonNull).map(String::valueOf)
                    .reduce((a, b) -> a + ", " + b).orElse("");
        }
        return String.valueOf(raw);
    }

    private static Integer asInteger(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(raw).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Double asDouble(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(raw).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String asString(Object raw) {
        return raw == null ? null : String.valueOf(raw);
    }
}
