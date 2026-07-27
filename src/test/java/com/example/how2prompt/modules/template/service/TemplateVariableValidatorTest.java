package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.modules.template.dto.FieldError;
import com.example.how2prompt.modules.template.entity.TemplateVariable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateVariableValidatorTest {

    private TemplateVariableValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TemplateVariableValidator();
    }

    @Test
    void validate_collectsAllErrors_notFailFast() {
        TemplateVariable name = variable("name", "text", true, Map.of("minLength", 2));
        TemplateVariable tone = variable("tone", "select", true, Map.of());
        tone.setOptions(List.of("formal", "casual"));
        TemplateVariable count = variable("count", "number", false, Map.of("min", 1, "max", 10));

        List<FieldError> errors = validator.validate(
                List.of(name, tone, count),
                Map.of(
                        "name", "A",
                        "tone", "aggressive",
                        "count", 99
                )
        );

        assertThat(errors).extracting(FieldError::field)
                .containsExactlyInAnyOrder("name", "tone", "count");
        assertThat(errors).extracting(FieldError::code)
                .contains("MIN_LENGTH", "INVALID_OPTION", "MAX");
    }

    @Test
    void validate_requiredMissing_reportsRequired() {
        TemplateVariable topic = variable("topic", "text", true, Map.of());

        List<FieldError> errors = validator.validate(List.of(topic), Map.of());

        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().code()).isEqualTo("REQUIRED");
        assertThat(errors.getFirst().field()).isEqualTo("topic");
    }

    @Test
    void validate_optionalMissing_ok() {
        TemplateVariable note = variable("note", "textarea", false, Map.of("maxLength", 100));

        List<FieldError> errors = validator.validate(List.of(note), Map.of());

        assertThat(errors).isEmpty();
    }

    @Test
    void validate_multiselect_rejectsUnknownOption() {
        TemplateVariable tags = variable("tags", "multiselect", true, Map.of());
        tags.setOptions(List.of(
                Map.of("value", "seo", "label", "SEO"),
                Map.of("value", "ads", "label", "Ads")
        ));

        List<FieldError> errors = validator.validate(
                List.of(tags),
                Map.of("tags", List.of("seo", "viral"))
        );

        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().code()).isEqualTo("INVALID_OPTION");
        assertThat(errors.getFirst().message()).contains("viral");
    }

    @Test
    void validate_regexAndHappyPath() {
        TemplateVariable email = variable("email", "text", true, Map.of(
                "regex", "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"
        ));

        assertThat(validator.validate(List.of(email), Map.of("email", "bad")))
                .extracting(FieldError::code)
                .containsExactly("REGEX");

        assertThat(validator.validate(List.of(email), Map.of("email", "a@b.co")))
                .isEmpty();
    }

    private static TemplateVariable variable(
            String key,
            String inputType,
            boolean required,
            Map<String, Object> validation
    ) {
        TemplateVariable v = new TemplateVariable();
        v.setVarKey(key);
        v.setInputType(inputType);
        v.setRequired(required);
        v.setValidation(validation);
        v.setLabelI18n(Map.of("en", key));
        return v;
    }
}
