package com.example.how2prompt.modules.catalog;

import com.example.how2prompt.modules.catalog.dto.request.CreateAiModelRequest;
import com.example.how2prompt.modules.catalog.dto.request.UpdateAiModelRequest;
import com.example.how2prompt.modules.catalog.dto.response.AiModelResponse;
import com.example.how2prompt.modules.catalog.entity.AiModel;
import com.example.how2prompt.modules.catalog.repository.AiModelRepository;
import com.example.how2prompt.modules.catalog.service.AiModelAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiModelAdminServiceTest {

    @Mock
    private AiModelRepository aiModelRepository;

    @InjectMocks
    private AiModelAdminService aiModelAdminService;

    private UUID modelId;
    private AiModel model;

    @BeforeEach
    void setUp() {
        modelId = UUID.randomUUID();
        model = new AiModel();
        model.setId(modelId);
        model.setCode("old-code");
        model.setName("Old Name");
    }

    @Test
    void testCreate_WithNullMaps_AndNullOptionals() {
        CreateAiModelRequest req = new CreateAiModelRequest();
        req.setCode("new-code");
        req.setName("New Name");
        req.setCapabilities(null);
        req.setDefaultConfig(null);
        req.setIsActive(null);
        req.setSortOrder(null);

        when(aiModelRepository.existsByCode("new-code")).thenReturn(false);
        when(aiModelRepository.save(any(AiModel.class))).thenAnswer(i -> i.getArgument(0));

        AiModelResponse res = aiModelAdminService.create(req);

        assertEquals("new-code", res.getCode());
        assertEquals("New Name", res.getName());
        assertNotNull(res.getCapabilities());
        assertTrue(res.getCapabilities().isEmpty());
        assertNotNull(res.getDefaultConfig());
        assertTrue(res.getDefaultConfig().isEmpty());
        // Default values for entity could be set, so we don't strictly assert null.
        
        verify(aiModelRepository).save(any(AiModel.class));
    }

    @Test
    void testCreate_AllFieldsPopulated() {
        CreateAiModelRequest req = new CreateAiModelRequest();
        req.setCode("full-code");
        req.setName("Full Name");
        req.setCapabilities(Map.of("k", "v"));
        req.setDefaultConfig(Map.of("k", "v"));
        req.setIsActive(true);
        req.setSortOrder(1);

        when(aiModelRepository.existsByCode("full-code")).thenReturn(false);
        when(aiModelRepository.save(any(AiModel.class))).thenAnswer(i -> i.getArgument(0));

        AiModelResponse res = aiModelAdminService.create(req);
        assertEquals(true, res.getIsActive());
        assertEquals(1, res.getSortOrder());
    }

    @Test
    void testCreate_Conflict() {
        CreateAiModelRequest req = new CreateAiModelRequest();
        req.setCode("conflict-code");
        when(aiModelRepository.existsByCode("conflict-code")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> aiModelAdminService.create(req));
    }

    @Test
    void testUpdate_AllFieldsPopulated() {
        UpdateAiModelRequest req = new UpdateAiModelRequest();
        req.setName("Updated Name");
        req.setProvider("Updated Provider");
        req.getModelType();
        req.setModelType("LLM");
        req.setDescription("Updated Desc");
        req.setCapabilities(Map.of("key", "val"));
        req.setDefaultConfig(Map.of("conf", "val"));
        req.setIconUrl("http://new-icon.png");
        req.setDocUrl("http://new-doc.png");
        req.setIsActive(false);
        req.setSortOrder(99);

        when(aiModelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(aiModelRepository.save(any(AiModel.class))).thenAnswer(i -> i.getArgument(0));

        AiModelResponse res = aiModelAdminService.update(modelId, req);

        assertEquals("Updated Name", res.getName());
        assertEquals("Updated Provider", res.getProvider());
        assertEquals("LLM", res.getModelType());
        assertEquals("Updated Desc", res.getDescription());
        assertEquals(Map.of("key", "val"), res.getCapabilities());
        assertEquals(Map.of("conf", "val"), res.getDefaultConfig());
        assertEquals("http://new-icon.png", res.getIconUrl());
        assertEquals("http://new-doc.png", res.getDocUrl());
        assertFalse(res.getIsActive());
        assertEquals(99, res.getSortOrder());
    }

    @Test
    void testUpdate_WithNullFields() {
        UpdateAiModelRequest req = new UpdateAiModelRequest();
        // All fields null

        when(aiModelRepository.findById(modelId)).thenReturn(Optional.of(model));
        when(aiModelRepository.save(any(AiModel.class))).thenAnswer(i -> i.getArgument(0));

        AiModelResponse res = aiModelAdminService.update(modelId, req);

        // Should retain old values
        assertEquals("Old Name", res.getName());
    }

    @Test
    void testUpdate_NotFound() {
        when(aiModelRepository.findById(modelId)).thenReturn(Optional.empty());
        UpdateAiModelRequest req = new UpdateAiModelRequest();
        
        assertThrows(RuntimeException.class, () -> aiModelAdminService.update(modelId, req));
    }
}
