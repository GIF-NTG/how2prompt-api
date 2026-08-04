package com.example.how2prompt.modules.template.controller;

import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.template.dto.*;
import com.example.how2prompt.modules.template.service.TemplateAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import com.example.how2prompt.config.AuthProperties;
import com.example.how2prompt.config.JwtProperties;
import com.example.how2prompt.infrastructure.security.JwtTokenProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class TemplateAdminControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TemplateAdminService templateAdminService;

    @BeforeEach
    void setUp() {
        TemplateAdminController controller = new TemplateAdminController(templateAdminService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(AuthenticatedUser.class);
                    }
                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return new AuthenticatedUser(UUID.randomUUID(), "admin@example.com", UUID.randomUUID(), true);
                    }
                })
                .build();
    }

    @Test
    void createTemplate_success() throws Exception {
        CreateTemplateRequest req = new CreateTemplateRequest();
        req.setSlug("test-slug");
        req.setTitleI18n(Map.of("en", "title"));
        req.setPromptBody("body");
        
        TemplateResponse res = new TemplateResponse(
                UUID.randomUUID(), null, "test-slug", null, null, null, null, null, false, false, "draft",
                null, 0L, 0, null, null, null, null, List.of(), List.of(), List.of()
        );
        
        when(templateAdminService.createTemplate(any(), any()))
                .thenReturn(res);

        mockMvc.perform(post("/admin/templates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.slug").value("test-slug"));
    }

    @Test
    void updateTemplate_success() throws Exception {
        UpdateTemplateRequest req = new UpdateTemplateRequest();
        req.setStatus("published");
        
        UUID id = UUID.randomUUID();
        TemplateResponse res = new TemplateResponse(
                id, null, "test-slug", null, null, null, null, null, false, false, "published",
                null, 0L, 0, null, null, null, null, List.of(), List.of(), List.of()
        );

        when(templateAdminService.updateTemplate(eq(id), any(UpdateTemplateRequest.class))).thenReturn(res);

        mockMvc.perform(patch("/admin/templates/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("published"));
    }

    @Test
    void addVariable_success() throws Exception {
        CreateVariableRequest req = new CreateVariableRequest();
        req.setVarKey("testKey");
        req.setInputType("text");
        req.setLabelI18n(Map.of("en", "label"));
        
        UUID id = UUID.randomUUID();
        TemplateVariableResponse res = new TemplateVariableResponse(
                UUID.randomUUID(), UUID.randomUUID(), "testKey", null, null, null, null, "text", false, null, List.of(), Map.of(), 1
        );

        when(templateAdminService.addVariable(eq(id), any(CreateVariableRequest.class))).thenReturn(res);

        mockMvc.perform(post("/admin/templates/{id}/variables", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.varKey").value("testKey"));
    }

    @Test
    void addVariant_success() throws Exception {
        CreateVariantRequest req = new CreateVariantRequest();
        req.setAiModelId(UUID.randomUUID());
        
        UUID id = UUID.randomUUID();
        TemplateVariantResponse res = new TemplateVariantResponse(
                UUID.randomUUID(), UUID.randomUUID(), req.getAiModelId(), null, null, Map.of(), Map.of()
        );

        when(templateAdminService.addVariant(eq(id), any(CreateVariantRequest.class))).thenReturn(res);

        mockMvc.perform(post("/admin/templates/{id}/variants", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.aiModelId").value(req.getAiModelId().toString()));
    }
}
