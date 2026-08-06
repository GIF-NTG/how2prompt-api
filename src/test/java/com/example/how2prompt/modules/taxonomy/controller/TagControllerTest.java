package com.example.how2prompt.modules.taxonomy.controller;

import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import com.example.how2prompt.modules.taxonomy.service.TagQueryService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TagQueryService tagQueryService;

    @BeforeEach
    void setUp() {
        TagController controller = new TagController(tagQueryService);
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
    void getPopularTags_Success() throws Exception {
        TagResponse res = new TagResponse();
        res.setSlug("test-tag");

        when(tagQueryService.findPopular(20)).thenReturn(List.of(res));

        mockMvc.perform(get("/tags")
                        .param("limit", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("test-tag"));
    }
}
