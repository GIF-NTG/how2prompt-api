package com.example.how2prompt.modules.taxonomy.controller;

import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateCategoryRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateTagRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.TagMergeRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.UpdateCategoryRequest;
import com.example.how2prompt.modules.taxonomy.dto.response.CategoryTreeResponse;
import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import com.example.how2prompt.modules.taxonomy.service.TaxonomyAdminService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaxonomyAdminControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TaxonomyAdminService taxonomyAdminService;

    @BeforeEach
    void setUp() {
        TaxonomyAdminController controller = new TaxonomyAdminController(taxonomyAdminService);
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
    void createCategory_Success() throws Exception {
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setSlug("cat");
        req.setNameI18n(Map.of("en", "Cat"));

        CategoryTreeResponse res = new CategoryTreeResponse();
        res.setSlug("cat");

        when(taxonomyAdminService.createCategory(any(CreateCategoryRequest.class))).thenReturn(res);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("cat"));
    }

    @Test
    void updateCategory_Success() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateCategoryRequest req = new UpdateCategoryRequest();
        req.setSlug("cat2");

        CategoryTreeResponse res = new CategoryTreeResponse();
        res.setSlug("cat2");

        when(taxonomyAdminService.updateCategory(eq(id), any(UpdateCategoryRequest.class))).thenReturn(res);

        mockMvc.perform(patch("/admin/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("cat2"));
    }

    @Test
    void deleteCategory_Success() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/admin/categories/{id}", id))
                .andExpect(status().isNoContent());
        verify(taxonomyAdminService).deleteCategory(id);
    }

    @Test
    void createTag_Success() throws Exception {
        CreateTagRequest req = new CreateTagRequest();
        req.setSlug("tag");
        req.setName("Tag");

        TagResponse res = new TagResponse();
        res.setSlug("tag");

        when(taxonomyAdminService.createTag(any(CreateTagRequest.class))).thenReturn(res);

        mockMvc.perform(post("/admin/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("tag"));
    }

    @Test
    void updateTag_Success() throws Exception {
        UUID id = UUID.randomUUID();
        CreateTagRequest req = new CreateTagRequest();
        req.setSlug("tag2");
        req.setName("Tag2");

        TagResponse res = new TagResponse();
        res.setSlug("tag2");

        when(taxonomyAdminService.updateTag(eq(id), any(CreateTagRequest.class))).thenReturn(res);

        mockMvc.perform(patch("/admin/tags/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("tag2"));
    }

    @Test
    void deleteTag_Success() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/admin/tags/{id}", id))
                .andExpect(status().isNoContent());
        verify(taxonomyAdminService).deleteTag(id);
    }

    @Test
    void mergeTags_Success() throws Exception {
        TagMergeRequest req = new TagMergeRequest();
        req.setSourceTagId(UUID.randomUUID());
        req.setTargetTagId(UUID.randomUUID());

        mockMvc.perform(post("/admin/tags/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        verify(taxonomyAdminService).mergeTags(any(TagMergeRequest.class));
    }
}
