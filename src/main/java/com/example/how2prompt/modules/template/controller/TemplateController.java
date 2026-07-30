package com.example.how2prompt.modules.template.controller;

import com.example.how2prompt.common.response.ApiResponse;
import com.example.how2prompt.common.security.AuthenticatedUser;
import com.example.how2prompt.common.security.CurrentUser;
import com.example.how2prompt.modules.template.dto.request.TemplateSearchCriteria;
import com.example.how2prompt.common.response.PageResponse;
import com.example.how2prompt.modules.template.dto.response.TemplateDetailResponse;
import com.example.how2prompt.modules.template.dto.response.TemplateSummaryResponse;
import com.example.how2prompt.modules.template.service.TemplateQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateQueryService templateQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TemplateSummaryResponse>>> searchTemplates(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "tag", required = false) List<String> tag,
            @RequestParam(name = "model", required = false) String model,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "sort", required = false, defaultValue = "newest") String sort,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "20") Integer limit,
            @Nullable @CurrentUser AuthenticatedUser currentUser
    ) {
        TemplateSearchCriteria criteria = new TemplateSearchCriteria();
        criteria.setCategory(category);
        criteria.setTags(tag);
        criteria.setModel(model);
        criteria.setSearch(search);
        criteria.setSort(sort);
        criteria.setCursor(cursor);
        criteria.setLimit(limit);

        PageResponse<TemplateSummaryResponse> page = templateQueryService.search(criteria, currentUser);
        return ResponseEntity.ok(ApiResponse.of(page));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<PageResponse<TemplateSummaryResponse>>> getFeaturedTemplates(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "tag", required = false) List<String> tag,
            @RequestParam(name = "model", required = false) String model,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "20") Integer limit,
            @Nullable @CurrentUser AuthenticatedUser currentUser
    ) {
        TemplateSearchCriteria criteria = new TemplateSearchCriteria();
        criteria.setCategory(category);
        criteria.setTags(tag);
        criteria.setModel(model);
        criteria.setSearch(search);
        criteria.setSort("featured");
        criteria.setCursor(cursor);
        criteria.setLimit(limit);

        PageResponse<TemplateSummaryResponse> page = templateQueryService.searchCached(criteria, currentUser);
        return ResponseEntity.ok(ApiResponse.of(page));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<PageResponse<TemplateSummaryResponse>>> getTrendingTemplates(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "tag", required = false) List<String> tag,
            @RequestParam(name = "model", required = false) String model,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", required = false, defaultValue = "20") Integer limit,
            @Nullable @CurrentUser AuthenticatedUser currentUser
    ) {
        TemplateSearchCriteria criteria = new TemplateSearchCriteria();
        criteria.setCategory(category);
        criteria.setTags(tag);
        criteria.setModel(model);
        criteria.setSearch(search);
        criteria.setSort("trending");
        criteria.setCursor(cursor);
        criteria.setLimit(limit);

        PageResponse<TemplateSummaryResponse> page = templateQueryService.searchCached(criteria, currentUser);
        return ResponseEntity.ok(ApiResponse.of(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateDetailResponse>> getTemplateDetail(
            @PathVariable("id") UUID id,
            @Nullable @CurrentUser AuthenticatedUser currentUser
    ) {
        UUID currentUserId = currentUser != null ? currentUser.userId() : null;
        boolean isAdmin = currentUser != null && currentUser.admin();

        TemplateDetailResponse detail = templateQueryService.getDetail(id, currentUserId, isAdmin);
        return ResponseEntity.ok(ApiResponse.of(detail));
    }
}
