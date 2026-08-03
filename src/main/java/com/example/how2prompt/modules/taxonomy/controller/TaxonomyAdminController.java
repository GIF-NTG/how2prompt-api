package com.example.how2prompt.modules.taxonomy.controller;

import com.example.how2prompt.modules.taxonomy.dto.request.CreateCategoryRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.CreateTagRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.TagMergeRequest;
import com.example.how2prompt.modules.taxonomy.dto.request.UpdateCategoryRequest;
import com.example.how2prompt.modules.taxonomy.dto.response.CategoryTreeResponse;
import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import com.example.how2prompt.modules.taxonomy.service.TaxonomyAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class TaxonomyAdminController {

    private final TaxonomyAdminService taxonomyAdminService;

    @PostMapping("/categories")
    public ResponseEntity<CategoryTreeResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryTreeResponse response = taxonomyAdminService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/categories/{id}")
    public ResponseEntity<CategoryTreeResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryTreeResponse response = taxonomyAdminService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        taxonomyAdminService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tags")
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody CreateTagRequest request) {
        TagResponse response = taxonomyAdminService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/tags/{id}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTagRequest request) {
        TagResponse response = taxonomyAdminService.updateTag(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        taxonomyAdminService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tags/merge")
    public ResponseEntity<Void> mergeTags(@Valid @RequestBody TagMergeRequest request) {
        taxonomyAdminService.mergeTags(request);
        return ResponseEntity.ok().build();
    }
}
