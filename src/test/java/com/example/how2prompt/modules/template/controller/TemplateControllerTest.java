package com.example.how2prompt.modules.template.controller;

import com.example.how2prompt.common.exception.GlobalExceptionHandler;
import com.example.how2prompt.common.response.PageResponse;
import com.example.how2prompt.common.security.AuthenticatedUser;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import com.example.how2prompt.modules.template.dto.request.TemplateSearchCriteria;
import com.example.how2prompt.modules.template.dto.response.TemplateDetailResponse;
import com.example.how2prompt.modules.template.dto.response.TemplateSummaryResponse;
import com.example.how2prompt.modules.template.service.FavoriteService;
import com.example.how2prompt.modules.template.service.TemplateQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TemplateControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private TemplateController templateController;

    @Mock
    private TemplateQueryService templateQueryService;

    @Mock
    private FavoriteService favoriteService;

    private AuthenticatedUser mockUser;
    
    private final HandlerMethodArgumentResolver authArgumentResolver = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().equals(AuthenticatedUser.class);
        }
        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return mockUser;
        }
    };

    @BeforeEach
    void setUp() {
        mockUser = new AuthenticatedUser(UUID.randomUUID(), "test@example.com", null, false);
        
        mockMvc = MockMvcBuilders.standaloneSetup(templateController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authArgumentResolver)
                .build();
    }
    
    private void setMockUser(AuthenticatedUser user) {
        mockUser = user;
    }

    @Test
    void searchTemplates_Success() throws Exception {
        setMockUser(mockUser);
        PageResponse<TemplateSummaryResponse> page = new PageResponse<>();
        when(templateQueryService.search(any(TemplateSearchCriteria.class), any())).thenReturn(page);

        mockMvc.perform(get("/templates"))
                .andExpect(status().isOk());
    }

    @Test
    void getFeaturedTemplates_Success() throws Exception {
        setMockUser(null);
        PageResponse<TemplateSummaryResponse> page = new PageResponse<>();
        when(templateQueryService.searchCached(any(TemplateSearchCriteria.class), isNull())).thenReturn(page);

        mockMvc.perform(get("/templates/featured"))
                .andExpect(status().isOk());
    }

    @Test
    void getTrendingTemplates_Success() throws Exception {
        setMockUser(null);
        PageResponse<TemplateSummaryResponse> page = new PageResponse<>();
        when(templateQueryService.searchCached(any(TemplateSearchCriteria.class), isNull())).thenReturn(page);

        mockMvc.perform(get("/templates/trending"))
                .andExpect(status().isOk());
    }

    @Test
    void getTemplateDetail_UserIsAdmin() throws Exception {
        AuthenticatedUser adminUser = new AuthenticatedUser(UUID.randomUUID(), "admin@example.com", null, true);
        setMockUser(adminUser);
        
        UUID templateId = UUID.randomUUID();
        TemplateDetailResponse response = new TemplateDetailResponse();
        response.setId(templateId);
        response.setTitleI18n(java.util.Map.of("en", "Test Template"));
                
        when(templateQueryService.getDetail(eq(templateId), eq(adminUser.userId()), eq(true))).thenReturn(response);

        mockMvc.perform(get("/templates/{id}", templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.titleI18n.en").value("Test Template"));
    }

    @Test
    void getTemplateDetail_UserIsNotAdmin() throws Exception {
        setMockUser(mockUser);
        
        UUID templateId = UUID.randomUUID();
        TemplateDetailResponse response = new TemplateDetailResponse();
        response.setId(templateId);
        response.setTitleI18n(java.util.Map.of("en", "Test Template User"));
                
        when(templateQueryService.getDetail(eq(templateId), eq(mockUser.userId()), eq(false))).thenReturn(response);

        mockMvc.perform(get("/templates/{id}", templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.titleI18n.en").value("Test Template User"));
    }

    @Test
    void getTemplateDetail_NoUser() throws Exception {
        setMockUser(null);
        
        UUID templateId = UUID.randomUUID();
        TemplateDetailResponse response = new TemplateDetailResponse();
        response.setId(templateId);
        response.setTitleI18n(java.util.Map.of("en", "Test Template No User"));
                
        when(templateQueryService.getDetail(eq(templateId), isNull(), eq(false))).thenReturn(response);

        mockMvc.perform(get("/templates/{id}", templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.titleI18n.en").value("Test Template No User"));
    }

    @Test
    void favorite_Success() throws Exception {
        setMockUser(mockUser);
        UUID templateId = UUID.randomUUID();

        mockMvc.perform(post("/templates/{id}/favorite", templateId))
                .andExpect(status().isOk());
                
        verify(favoriteService).addFavorite(templateId, mockUser.userId());
    }

    @Test
    void favorite_NoUser_Returns401() throws Exception {
        setMockUser(null);
        UUID templateId = UUID.randomUUID();

        mockMvc.perform(post("/templates/{id}/favorite", templateId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unfavorite_Success() throws Exception {
        setMockUser(mockUser);
        UUID templateId = UUID.randomUUID();

        mockMvc.perform(delete("/templates/{id}/favorite", templateId))
                .andExpect(status().isNoContent());
                
        verify(favoriteService).removeFavorite(templateId, mockUser.userId());
    }

    @Test
    void unfavorite_NoUser_Returns401() throws Exception {
        setMockUser(null);
        UUID templateId = UUID.randomUUID();

        mockMvc.perform(delete("/templates/{id}/favorite", templateId))
                .andExpect(status().isUnauthorized());
    }
}
