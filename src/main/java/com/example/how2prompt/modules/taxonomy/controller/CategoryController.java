package com.example.how2prompt.modules.taxonomy.controller;

import com.example.how2prompt.modules.taxonomy.dto.response.CategoryTreeResponse;
import com.example.how2prompt.modules.taxonomy.service.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryQueryService categoryQueryService;

    @GetMapping
    public ResponseEntity<List<CategoryTreeResponse>> getCategoryTree() {
        List<CategoryTreeResponse> tree = categoryQueryService.findTree();
        return ResponseEntity.ok(tree);
    }
}
