package com.example.how2prompt.modules.template.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TemplateSearchCriteria {
    private String category;
    private List<String> tags;
    private String model;
    private String search;
    private String sort = "newest";
    private String cursor;
    private Integer limit = 20;
}
