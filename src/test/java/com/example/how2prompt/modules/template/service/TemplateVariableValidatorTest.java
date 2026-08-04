package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.modules.template.dto.FieldError;
import com.example.how2prompt.modules.template.entity.TemplateVariable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateVariableValidatorTest {

    private TemplateVariableValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TemplateVariableValidator();
    }

    // --- null/empty inputs ---
    @Test
    void validate_nullVariables_returnsEmpty() {
        assertThat(validator.validate(null, Map.of())).isEmpty();
        assertThat(validator.validate(List.of(), null)).isEmpty();
    }

    // --- isMissing ECP ---
    @Test
    void validate_isMissing_variousTypes() {
        TemplateVariable v1 = variable("v1", "text", true, Map.of());
        TemplateVariable v2 = variable("v2", "text", true, Map.of());
        TemplateVariable v3 = variable("v3", "text", true, Map.of());
        TemplateVariable v4 = variable("v4", "text", true, Map.of());

        List<FieldError> errors = validator.validate(
                List.of(v1, v2, v3, v4),
                Map.of(
                        "v1", "", // empty string
                        "v2", List.of(), // empty collection
                        "v3", Map.of() // empty map
                        // v4 is totally missing (null implicitly)
                )
        );

        assertThat(errors).hasSize(4);
        assertThat(errors).extracting(FieldError::field).containsExactlyInAnyOrder("v1", "v2", "v3", "v4");
        assertThat(errors).extracting(FieldError::code).containsOnly("REQUIRED");
    }

    // --- validateText BVA & ECP ---
    @Test
    void validateText_minLength_BVA() {
        TemplateVariable v = variable("textVar", "text", true, Map.of("minLength", 5));
        
        // < minLength
        assertThat(validator.validate(List.of(v), Map.of("textVar", "1234")))
                .extracting(FieldError::code).containsExactly("MIN_LENGTH");
        
        // == minLength
        assertThat(validator.validate(List.of(v), Map.of("textVar", "12345"))).isEmpty();
        
        // > minLength
        assertThat(validator.validate(List.of(v), Map.of("textVar", "123456"))).isEmpty();
    }

    @Test
    void validateText_maxLength_BVA() {
        TemplateVariable v = variable("textVar", "text", true, Map.of("maxLength", 5));
        
        // < maxLength
        assertThat(validator.validate(List.of(v), Map.of("textVar", "1234"))).isEmpty();
        
        // == maxLength
        assertThat(validator.validate(List.of(v), Map.of("textVar", "12345"))).isEmpty();
        
        // > maxLength
        assertThat(validator.validate(List.of(v), Map.of("textVar", "123456")))
                .extracting(FieldError::code).containsExactly("MAX_LENGTH");
    }

    @Test
    void validateText_regex() {
        TemplateVariable v = variable("textVar", "text", true, Map.of("regex", "^[a-z]+$"));
        
        // Invalid value
        assertThat(validator.validate(List.of(v), Map.of("textVar", "123")))
                .extracting(FieldError::code).containsExactly("REGEX");
        
        // Valid value
        assertThat(validator.validate(List.of(v), Map.of("textVar", "abc"))).isEmpty();
        
        // Invalid regex syntax in config
        TemplateVariable badRegex = variable("bad", "text", true, Map.of("regex", "[invalid"));
        assertThat(validator.validate(List.of(badRegex), Map.of("bad", "abc")))
                .extracting(FieldError::code).containsExactly("REGEX");
        assertThat(validator.validate(List.of(badRegex), Map.of("bad", "abc")))
                .extracting(FieldError::message).containsExactly("Cấu hình regex không hợp lệ.");
    }
    
    @Test
    void validateText_collectionValue() {
        // test stringify of collection ("a, b" length is 4)
        TemplateVariable v = variable("textVar", "text", true, Map.of("minLength", 4));
        assertThat(validator.validate(List.of(v), Map.of("textVar", List.of("a", "b")))).isEmpty();
    }

    // --- validateNumber BVA & ECP ---
    @Test
    void validateNumber_typeCheck() {
        TemplateVariable v = variable("num", "number", true, Map.of());
        
        // valid number
        assertThat(validator.validate(List.of(v), Map.of("num", 10))).isEmpty();
        assertThat(validator.validate(List.of(v), Map.of("num", 10.5))).isEmpty();
        assertThat(validator.validate(List.of(v), Map.of("num", "10"))).isEmpty();
        
        // invalid type
        assertThat(validator.validate(List.of(v), Map.of("num", "abc")))
                .extracting(FieldError::code).containsExactly("INVALID_TYPE");
        assertThat(validator.validate(List.of(v), Map.of("num", List.of(1))))
                .extracting(FieldError::code).containsExactly("INVALID_TYPE");
    }

    @Test
    void validateNumber_min_BVA() {
        TemplateVariable v = variable("num", "number", true, Map.of("min", 10));
        
        // < min
        assertThat(validator.validate(List.of(v), Map.of("num", 9.9)))
                .extracting(FieldError::code).containsExactly("MIN");
        
        // == min
        assertThat(validator.validate(List.of(v), Map.of("num", 10))).isEmpty();
        
        // > min
        assertThat(validator.validate(List.of(v), Map.of("num", 10.1))).isEmpty();
    }

    @Test
    void validateNumber_max_BVA() {
        TemplateVariable v = variable("num", "number", true, Map.of("max", 10));
        
        // < max
        assertThat(validator.validate(List.of(v), Map.of("num", 9.9))).isEmpty();
        
        // == max
        assertThat(validator.validate(List.of(v), Map.of("num", 10))).isEmpty();
        
        // > max
        assertThat(validator.validate(List.of(v), Map.of("num", 10.1)))
                .extracting(FieldError::code).containsExactly("MAX");
    }
    
    @Test
    void validateNumber_slider() {
        TemplateVariable v = variable("slider", "slider", true, Map.of("max", 10));
        assertThat(validator.validate(List.of(v), Map.of("slider", 11)))
                .extracting(FieldError::code).containsExactly("MAX");
    }

    // --- validateBoolean ECP ---
    @Test
    void validateBoolean_validTypes() {
        TemplateVariable v = variable("bool", "boolean", true, Map.of());
        
        assertThat(validator.validate(List.of(v), Map.of("bool", true))).isEmpty();
        assertThat(validator.validate(List.of(v), Map.of("bool", false))).isEmpty();
        
        assertThat(validator.validate(List.of(v), Map.of("bool", "true"))).isEmpty();
        assertThat(validator.validate(List.of(v), Map.of("bool", "false"))).isEmpty();
        assertThat(validator.validate(List.of(v), Map.of("bool", "1"))).isEmpty();
        assertThat(validator.validate(List.of(v), Map.of("bool", "0"))).isEmpty();
        
        assertThat(validator.validate(List.of(v), Map.of("bool", 1))).isEmpty();
        assertThat(validator.validate(List.of(v), Map.of("bool", 0))).isEmpty();
        assertThat(validator.validate(List.of(v), Map.of("bool", 1.0))).isEmpty();
        assertThat(validator.validate(List.of(v), Map.of("bool", 0.0))).isEmpty();
    }

    @Test
    void validateBoolean_invalidTypes() {
        TemplateVariable v = variable("bool", "boolean", true, Map.of());
        
        assertThat(validator.validate(List.of(v), Map.of("bool", "yes")))
                .extracting(FieldError::code).containsExactly("INVALID_TYPE");
        assertThat(validator.validate(List.of(v), Map.of("bool", 2)))
                .extracting(FieldError::code).containsExactly("INVALID_TYPE");
        assertThat(validator.validate(List.of(v), Map.of("bool", 1.5)))
                .extracting(FieldError::code).containsExactly("INVALID_TYPE");
        assertThat(validator.validate(List.of(v), Map.of("bool", List.of(true))))
                .extracting(FieldError::code).containsExactly("INVALID_TYPE");
    }

    // --- validateSelect ECP ---
    @Test
    void validateSelect_emptyOptions() {
        TemplateVariable v = variable("sel", "select", true, Map.of());
        v.setOptions(List.of()); // Empty options configured
        
        // should not block end user if template is misconfigured
        assertThat(validator.validate(List.of(v), Map.of("sel", "anything"))).isEmpty();
        
        v.setOptions(null);
        assertThat(validator.validate(List.of(v), Map.of("sel", "anything"))).isEmpty();
    }

    @Test
    void validateSelect_validAndInvalid() {
        TemplateVariable v = variable("sel", "select", true, Map.of());
        v.setOptions(List.of("opt1", Map.of("value", "opt2"), Map.of("id", "opt3"), Map.of("label", "noValueOrId")));
        
        assertThat(validator.validate(List.of(v), Map.of("sel", "opt1"))).isEmpty();
        assertThat(validator.validate(List.of(v), Map.of("sel", "opt2"))).isEmpty();
        assertThat(validator.validate(List.of(v), Map.of("sel", "opt3"))).isEmpty();
        
        assertThat(validator.validate(List.of(v), Map.of("sel", "invalid")))
                .extracting(FieldError::code).containsExactly("INVALID_OPTION");
    }

    // --- validateMultiselect ECP ---
    @Test
    void validateMultiselect_emptyOptions() {
        TemplateVariable v = variable("msel", "multiselect", true, Map.of());
        v.setOptions(List.of());
        assertThat(validator.validate(List.of(v), Map.of("msel", List.of("any")))).isEmpty();
    }

    @Test
    void validateMultiselect_invalidType() {
        TemplateVariable v = variable("msel", "multiselect", true, Map.of());
        v.setOptions(List.of("opt1"));
        
        // Pass map which is not a valid list of strings
        assertThat(validator.validate(List.of(v), Map.of("msel", Map.of("a", "b"))))
                .extracting(FieldError::code).containsExactly("INVALID_TYPE");
    }

    @Test
    void validateMultiselect_validAndInvalid() {
        TemplateVariable v = variable("msel", "multiselect", true, Map.of());
        v.setOptions(List.of("opt1", "opt2"));
        
        // Single value
        assertThat(validator.validate(List.of(v), Map.of("msel", "opt1"))).isEmpty();
        
        // Collection
        assertThat(validator.validate(List.of(v), Map.of("msel", List.of("opt1", "opt2")))).isEmpty();
        
        // Array
        assertThat(validator.validate(List.of(v), Map.of("msel", new Object[]{"opt1"}))).isEmpty();
        
        // Contains invalid
        assertThat(validator.validate(List.of(v), Map.of("msel", List.of("opt1", "opt3"))))
                .extracting(FieldError::code).containsExactly("INVALID_OPTION");
                
        // null elements in option lists shouldn't cause NPE, but will be ignored
        v.setOptions(java.util.Arrays.asList("opt1", null));
        assertThat(validator.validate(List.of(v), Map.of("msel", "any")))
                .extracting(FieldError::code).containsExactly("INVALID_OPTION");
    }
    
    // --- normalizeType ---
    @Test
    void normalizeType_null_defaultsToText() {
        TemplateVariable v = variable("v", null, true, Map.of());
        // implicitly tests validateText since type becomes "text"
        assertThat(validator.validate(List.of(v), Map.of("v", "val"))).isEmpty();
    }
    
    @Test
    void normalizeType_upperCase() {
        TemplateVariable v = variable("v", "  BOOLEAN  ", true, Map.of());
        assertThat(validator.validate(List.of(v), Map.of("v", true))).isEmpty();
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
