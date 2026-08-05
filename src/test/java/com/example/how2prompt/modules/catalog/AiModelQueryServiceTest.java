package com.example.how2prompt.modules.catalog;

import com.example.how2prompt.common.exception.BadRequestException;
import com.example.how2prompt.common.exception.ResourceNotFoundException;
import com.example.how2prompt.modules.catalog.dto.response.AiModelSummaryResponse;
import com.example.how2prompt.modules.catalog.entity.AiModel;
import com.example.how2prompt.modules.catalog.repository.AiModelRepository;
import com.example.how2prompt.modules.catalog.service.AiModelQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiModelQueryServiceTest {

    @Mock
    private AiModelRepository aiModelRepository;

    @InjectMocks
    private AiModelQueryService aiModelQueryService;

    private UUID modelId;
    private AiModel model;

    @BeforeEach
    void setUp() {
        modelId = UUID.randomUUID();
        model = new AiModel();
        model.setId(modelId);
        model.setCode("test-model");
        model.setName("Test Model");
        model.setIsActive(true);
    }

    @Test
    void testFindActive() {
        when(aiModelRepository.findByIsActiveTrue()).thenReturn(List.of(model));
        List<AiModelSummaryResponse> res = aiModelQueryService.findActive();
        assertEquals(1, res.size());
        assertEquals(modelId, res.get(0).getId());
    }

    @Test
    void testFindByIds() {
        when(aiModelRepository.findAllById(Set.of(modelId))).thenReturn(List.of(model));
        List<AiModelSummaryResponse> res = aiModelQueryService.findByIds(Set.of(modelId));
        assertEquals(1, res.size());
    }

    @Test
    void testGetByIdOrThrow_Success() {
        when(aiModelRepository.findById(modelId)).thenReturn(Optional.of(model));
        AiModelSummaryResponse res = aiModelQueryService.getByIdOrThrow(modelId);
        assertEquals(modelId, res.getId());
    }

    @Test
    void testGetByIdOrThrow_NotFound() {
        when(aiModelRepository.findById(modelId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> aiModelQueryService.getByIdOrThrow(modelId));
    }

    @Test
    void testGetActiveByIdOrThrow_Success() {
        when(aiModelRepository.findById(modelId)).thenReturn(Optional.of(model));
        AiModelSummaryResponse res = aiModelQueryService.getActiveByIdOrThrow(modelId);
        assertEquals(modelId, res.getId());
    }

    @Test
    void testGetActiveByIdOrThrow_Inactive() {
        model.setIsActive(false);
        when(aiModelRepository.findById(modelId)).thenReturn(Optional.of(model));
        assertThrows(BadRequestException.class, () -> aiModelQueryService.getActiveByIdOrThrow(modelId));
    }

    @Test
    void testResolveIdByCode_NullOrBlank() {
        assertNull(aiModelQueryService.resolveIdByCode(null));
        assertNull(aiModelQueryService.resolveIdByCode("  "));
    }

    @Test
    void testResolveIdByCode_Success() {
        when(aiModelRepository.findByCode("test-model")).thenReturn(Optional.of(model));
        UUID id = aiModelQueryService.resolveIdByCode("test-model");
        assertEquals(modelId, id);
    }

    @Test
    void testResolveIdByCode_NotFound() {
        when(aiModelRepository.findByCode("unknown")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> aiModelQueryService.resolveIdByCode("unknown"));
    }

    @Test
    void testFindAll() {
        when(aiModelRepository.findAll()).thenReturn(List.of(model));
        List<AiModelSummaryResponse> res = aiModelQueryService.findAll();
        assertEquals(1, res.size());
    }
}
