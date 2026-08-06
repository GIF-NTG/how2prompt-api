package com.example.how2prompt.modules.catalog.controller;

import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.catalog.dto.request.CreateAiModelRequest;
import com.example.how2prompt.modules.catalog.dto.request.UpdateAiModelRequest;
import com.example.how2prompt.modules.catalog.dto.response.AiModelResponse;
import com.example.how2prompt.modules.catalog.service.AiModelAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AiModelAdminControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AiModelAdminService aiModelAdminService;

    @BeforeEach
    void setUp() {
        AiModelAdminController controller = new AiModelAdminController(aiModelAdminService);
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
    void createAiModel_Success() throws Exception {
        CreateAiModelRequest req = new CreateAiModelRequest();
        req.setCode("model-code");
        req.setName("Model Name");
        req.setProvider("Provider");
        req.setModelType("LLM");

        AiModelResponse res = new AiModelResponse();
        res.setId(UUID.randomUUID());
        res.setCode("model-code");
        res.setName("Model Name");
        res.setProvider("Provider");
        res.setModelType("LLM");
        res.setIsActive(true);

        when(aiModelAdminService.create(any(CreateAiModelRequest.class))).thenReturn(res);

        mockMvc.perform(post("/admin/ai-models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("model-code"));
    }

    @Test
    void createAiModel_ValidationError() throws Exception {
        CreateAiModelRequest req = new CreateAiModelRequest();
        // Missing required fields like code, name, provider, modelType

        mockMvc.perform(post("/admin/ai-models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest()); // MockMvc standalone might not have the GlobalExceptionHandler by default, but it will return 400 for @Valid
    }

    @Test
    void updateAiModel_Success() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateAiModelRequest req = new UpdateAiModelRequest();
        req.setName("Updated Name");

        AiModelResponse res = new AiModelResponse();
        res.setId(id);
        res.setCode("model-code");
        res.setName("Updated Name");
        res.setProvider("Provider");
        res.setModelType("LLM");
        res.setIsActive(true);

        when(aiModelAdminService.update(eq(id), any(UpdateAiModelRequest.class))).thenReturn(res);

        mockMvc.perform(patch("/admin/ai-models/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }
}
