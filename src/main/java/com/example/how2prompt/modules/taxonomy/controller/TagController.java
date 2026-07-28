package com.example.how2prompt.modules.taxonomy.controller;

import com.example.how2prompt.modules.taxonomy.dto.response.TagResponse;
import com.example.how2prompt.modules.taxonomy.service.TagQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TagController {

    private final TagQueryService tagQueryService;

    @GetMapping("/tags")
    public ResponseEntity<List<TagResponse>> getPopularTags(
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        List<TagResponse> tags = tagQueryService.findPopular(limit);
        return ResponseEntity.ok(tags);
    }
}
