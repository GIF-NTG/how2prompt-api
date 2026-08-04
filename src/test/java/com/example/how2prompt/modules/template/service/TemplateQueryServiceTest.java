package com.example.how2prompt.modules.template.service;

import com.example.how2prompt.common.exception.BadRequestException;
import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.template.dto.TemplateVersionStatus;
import com.example.how2prompt.modules.template.dto.request.TemplateSearchCriteria;
import com.example.how2prompt.modules.template.entity.Template;
import com.example.how2prompt.modules.template.entity.TemplateVersion;
import com.example.how2prompt.modules.template.repository.*;
import com.example.how2prompt.modules.catalog.service.AiModelQueryService;
import com.example.how2prompt.modules.taxonomy.service.CategoryQueryService;
import com.example.how2prompt.modules.taxonomy.service.TagQueryService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateQueryServiceTest {

    @Mock private TemplateRepository templateRepository;
    @Mock private TemplateVersionRepository templateVersionRepository;
    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private ObjectMapper objectMapper;
    
    @Mock private CategoryQueryService categoryQueryService;
    @Mock private TagQueryService tagQueryService;
    @Mock private AiModelQueryService aiModelQueryService;
    @Mock private TemplateUsageService templateUsageService;

    @InjectMocks
    private TemplateQueryService service;

    @Test
    void search_invalidLimit_throws() {
        TemplateSearchCriteria criteria = new TemplateSearchCriteria();
        criteria.setLimit(0);
        assertThrows(BadRequestException.class, () -> service.search(criteria, false));
        
        criteria.setLimit(101);
        assertThrows(BadRequestException.class, () -> service.search(criteria, false));
    }

    @Test
    void getDetail_unauthorized_throws() {
        Template t = new Template();
        t.setId(UUID.randomUUID());
        t.setAuthorId(UUID.randomUUID());
        t.setStatus("draft");
        t.setPublic(false);
        when(templateRepository.findById(t.getId())).thenReturn(Optional.of(t));

        assertThrows(ResourceNotFoundException.class, () -> service.getDetail(t.getId(), UUID.randomUUID(), false));
    }

    @Test
    void checkTemplateVersionStatus_templateNull_returnsDeleted() {
        TemplateVersionStatus status = service.checkTemplateVersionStatus(null, UUID.randomUUID());
        assertThat(status.templateDeleted()).isTrue();
    }

    @Test
    void checkTemplateVersionStatus_templateNotFound_returnsDeleted() {
        when(templateRepository.findById(any())).thenReturn(Optional.empty());
        TemplateVersionStatus status = service.checkTemplateVersionStatus(UUID.randomUUID(), UUID.randomUUID());
        assertThat(status.templateDeleted()).isTrue();
    }

    @Test
    void checkTemplateVersionStatus_noCurrentVersion_returnsNotDeleted() {
        Template t = new Template();
        when(templateRepository.findById(any())).thenReturn(Optional.of(t));
        TemplateVersionStatus status = service.checkTemplateVersionStatus(UUID.randomUUID(), UUID.randomUUID());
        assertThat(status.templateDeleted()).isFalse();
        assertThat(status.newerVersionAvailable()).isFalse();
    }

    @Test
    void checkTemplateVersionStatus_sameVersion_returnsUpToDate() {
        Template t = new Template();
        UUID vId = UUID.randomUUID();
        t.setCurrentVersionId(vId);
        when(templateRepository.findById(any())).thenReturn(Optional.of(t));

        TemplateVersionStatus status = service.checkTemplateVersionStatus(UUID.randomUUID(), vId);
        assertThat(status.templateDeleted()).isFalse();
        assertThat(status.newerVersionAvailable()).isFalse();
    }

    @Test
    void checkTemplateVersionStatus_outdated() {
        Template t = new Template();
        UUID currId = UUID.randomUUID();
        t.setCurrentVersionId(currId);
        when(templateRepository.findById(any())).thenReturn(Optional.of(t));

        TemplateVersion hist = new TemplateVersion();
        hist.setVersionNumber(1);
        TemplateVersion curr = new TemplateVersion();
        curr.setVersionNumber(2);

        UUID histId = UUID.randomUUID();
        when(templateVersionRepository.findById(histId)).thenReturn(Optional.of(hist));
        when(templateVersionRepository.findById(currId)).thenReturn(Optional.of(curr));

        TemplateVersionStatus status = service.checkTemplateVersionStatus(UUID.randomUUID(), histId);
        assertThat(status.newerVersionAvailable()).isTrue();
        assertThat(status.latestVersionNumber()).isEqualTo("v2");
    }
}
