package com.example.how2prompt.modules.catalog.controller;

import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.catalog.dto.response.AiModelSummaryResponse;
import com.example.how2prompt.modules.catalog.service.AiModelQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AiModelControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AiModelQueryService aiModelQueryService;

    @BeforeEach
    void setUp() {
        AiModelController controller = new AiModelController(aiModelQueryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(AuthenticatedUser.class);
                    }
                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return new AuthenticatedUser(UUID.randomUUID(), "user@example.com", UUID.randomUUID(), false);
                    }
                })
                .build();
    }

    @Test
    void getAiModels_Admin() throws Exception {
        AiModelSummaryResponse res = new AiModelSummaryResponse(UUID.randomUUID(), "model1", "Model 1", null);
        when(aiModelQueryService.findAll()).thenReturn(List.of(res));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        mockMvc.perform(get("/ai-models")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("model1"));
    }

    @Test
    void getAiModels_User() throws Exception {
        AiModelSummaryResponse res = new AiModelSummaryResponse(UUID.randomUUID(), "model2", "Model 2", null);
        when(aiModelQueryService.findActive()).thenReturn(List.of(res));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        mockMvc.perform(get("/ai-models")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("model2"));
    }
}
