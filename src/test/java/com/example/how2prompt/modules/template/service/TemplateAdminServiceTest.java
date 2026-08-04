package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.common.exception.BadRequestException;
import com.example.how2prompt.common.exception.ConflictException;
import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.modules.catalog.service.AiModelQueryService;
import com.example.how2prompt.modules.template.dto.CreateTemplateRequest;
import com.example.how2prompt.modules.template.dto.CreateVariableRequest;
import com.example.how2prompt.modules.template.dto.CreateVariantRequest;
import com.example.how2prompt.modules.template.dto.UpdateTemplateRequest;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.entity.TemplateModel;
import com.example.how2prompt.modules.template.entity.TemplateModelId;
import com.example.how2prompt.modules.template.entity.TemplateVariable;
import com.example.how2prompt.modules.template.entity.TemplateVariant;
import com.example.how2prompt.modules.template.entity.TemplateVersion;
import com.example.how2prompt.modules.template.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateAdminServiceTest {

    @Mock private TemplateRepository templateRepository;
    @Mock private TemplateVersionRepository templateVersionRepository;
    @Mock private TemplateVariableRepository templateVariableRepository;
    @Mock private TemplateVariantRepository templateVariantRepository;
    @Mock private TemplateCategoryRepository templateCategoryRepository;
    @Mock private TemplateTagRepository templateTagRepository;
    @Mock private TemplateModelRepository templateModelRepository;
    @Mock private AiModelQueryService aiModelQueryService;

    @InjectMocks
    private TemplateAdminService templateAdminService;

    private AuthenticatedUser createUser() {
        return new AuthenticatedUser(UUID.randomUUID(), "test@test.com", UUID.randomUUID(), false);
    }

    @Test
    void createTemplate_requireWorkspace_throws() {
        assertThrows(BadRequestException.class, () -> templateAdminService.createTemplate(new CreateTemplateRequest(), null));
        assertThrows(BadRequestException.class, () -> templateAdminService.createTemplate(new CreateTemplateRequest(), new AuthenticatedUser(UUID.randomUUID(), "", null, false)));
        assertThrows(BadRequestException.class, () -> templateAdminService.createTemplate(new CreateTemplateRequest(), new AuthenticatedUser(null, "", UUID.randomUUID(), false)));
    }

    @Test
    void createTemplate_slugExists_throwsConflict() {
        AuthenticatedUser user = createUser();
        CreateTemplateRequest req = new CreateTemplateRequest();
        req.setSlug("test-slug");
        when(templateRepository.existsByWorkspaceIdAndSlug(user.workspaceId(), "test-slug")).thenReturn(true);
        assertThrows(ConflictException.class, () -> templateAdminService.createTemplate(req, user));
    }

    @Test
    void updateTemplate_notFound_throws() {
        when(templateRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> templateAdminService.updateTemplate(UUID.randomUUID(), new UpdateTemplateRequest()));
    }

    @Test
    void updateTemplate_emptyTitleI18n_throws() {
        Template t = new Template();
        when(templateRepository.findById(any())).thenReturn(Optional.of(t));
        UpdateTemplateRequest req = new UpdateTemplateRequest();
        req.setTitleI18n(Map.of());
        assertThrows(BadRequestException.class, () -> templateAdminService.updateTemplate(UUID.randomUUID(), req));
    }
    
    @Test
    void updateTemplate_invalidStatus_throws() {
        Template t = new Template();
        when(templateRepository.findById(any())).thenReturn(Optional.of(t));
        UpdateTemplateRequest req = new UpdateTemplateRequest();
        req.setStatus("invalid-status");
        assertThrows(BadRequestException.class, () -> templateAdminService.updateTemplate(UUID.randomUUID(), req));
    }

    @Test
    void updateTemplate_statusDraft_clearsPublishedAt() {
        Template t = new Template();
        t.setId(UUID.randomUUID());
        t.setPublishedAt(java.time.Instant.now());
        when(templateRepository.findById(any())).thenReturn(Optional.of(t));
        when(templateRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdateTemplateRequest req = new UpdateTemplateRequest();
        req.setStatus("draft");
        var res = templateAdminService.updateTemplate(t.getId(), req);
        assertThat(res.status()).isEqualTo("draft");
        assertThat(res.publishedAt()).isNull();
    }

    @Test
    void updateTemplate_updatePrimaryModel_noModelsFound() {
        Template t = new Template();
        t.setId(UUID.randomUUID());
        when(templateRepository.findById(any())).thenReturn(Optional.of(t));
        when(templateModelRepository.findByIdTemplateId(t.getId())).thenReturn(List.of());

        UpdateTemplateRequest req = new UpdateTemplateRequest();
        req.setPrimaryModelId(UUID.randomUUID());
        assertThrows(BadRequestException.class, () -> templateAdminService.updateTemplate(t.getId(), req));
    }

    @Test
    void updateTemplate_updatePrimaryModel_notFoundInExisting() {
        Template t = new Template();
        t.setId(UUID.randomUUID());
        when(templateRepository.findById(any())).thenReturn(Optional.of(t));
        
        TemplateModel m = new TemplateModel();
        m.setId(new TemplateModelId(t.getId(), UUID.randomUUID()));
        when(templateModelRepository.findByIdTemplateId(t.getId())).thenReturn(List.of(m));

        UpdateTemplateRequest req = new UpdateTemplateRequest();
        req.setPrimaryModelId(UUID.randomUUID());
        assertThrows(BadRequestException.class, () -> templateAdminService.updateTemplate(t.getId(), req));
    }
    
    @Test
    void updateTemplate_updatePrimaryModel_success() {
        Template t = new Template();
        t.setId(UUID.randomUUID());
        when(templateRepository.findById(any())).thenReturn(Optional.of(t));
        when(templateRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        UUID modelId = UUID.randomUUID();
        TemplateModel m = new TemplateModel();
        m.setId(new TemplateModelId(t.getId(), modelId));
        when(templateModelRepository.findByIdTemplateId(t.getId())).thenReturn(List.of(m));

        UpdateTemplateRequest req = new UpdateTemplateRequest();
        req.setPrimaryModelId(modelId);
        templateAdminService.updateTemplate(t.getId(), req);
        verify(templateModelRepository).saveAll(any());
        assertThat(m.isPrimary()).isTrue();
    }

    @Test
    void addVariable_varKeyExists_throwsConflict() {
        Template t = new Template();
        t.setId(UUID.randomUUID());
        t.setCurrentVersionId(UUID.randomUUID());
        when(templateRepository.findById(any())).thenReturn(Optional.of(t));
        
        TemplateVersion tv = new TemplateVersion();
        tv.setId(t.getCurrentVersionId());
        when(templateVersionRepository.findByIdAndTemplateId(any(), any())).thenReturn(Optional.of(tv));
        when(templateVariableRepository.findByTemplateVersionIdAndVarKey(tv.getId(), "key")).thenReturn(Optional.of(new TemplateVariable()));

        CreateVariableRequest req = new CreateVariableRequest();
        req.setVarKey("key");
        assertThrows(ConflictException.class, () -> templateAdminService.addVariable(t.getId(), req));
    }

    @Test
    void addVariant_aiModelExists_throwsConflict() {
        Template t = new Template();
        t.setId(UUID.randomUUID());
        t.setCurrentVersionId(UUID.randomUUID());
        when(templateRepository.findById(any())).thenReturn(Optional.of(t));
        
        TemplateVersion tv = new TemplateVersion();
        tv.setId(t.getCurrentVersionId());
        when(templateVersionRepository.findByIdAndTemplateId(any(), any())).thenReturn(Optional.of(tv));
        
        UUID modelId = UUID.randomUUID();
        when(templateVariantRepository.findByTemplateVersionIdAndAiModelId(tv.getId(), modelId)).thenReturn(Optional.of(new TemplateVariant()));

        CreateVariantRequest req = new CreateVariantRequest();
        req.setAiModelId(modelId);
        assertThrows(ConflictException.class, () -> templateAdminService.addVariant(t.getId(), req));
    }

    @Test
    void getCurrentVersionOrThrow_noCurrentVersionId() {
        Template t = new Template();
        t.setId(UUID.randomUUID());
        when(templateRepository.findById(any())).thenReturn(Optional.of(t));

        CreateVariableRequest req = new CreateVariableRequest();
        req.setVarKey("key");
        assertThrows(BadRequestException.class, () -> templateAdminService.addVariable(t.getId(), req));
    }

    @Test
    void getCurrentVersionOrThrow_notFoundFallbackFail() {
        Template t = new Template();
        t.setId(UUID.randomUUID());
        t.setCurrentVersionId(UUID.randomUUID());
        when(templateRepository.findById(any())).thenReturn(Optional.of(t));
        when(templateVersionRepository.findByIdAndTemplateId(any(), any())).thenReturn(Optional.empty());
        when(templateVersionRepository.findByTemplateIdAndCurrentTrue(any())).thenReturn(Optional.empty());

        CreateVariableRequest req = new CreateVariableRequest();
        req.setVarKey("key");
        assertThrows(BadRequestException.class, () -> templateAdminService.addVariable(t.getId(), req));
    }
}
