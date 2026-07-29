package com.example.how2prompt.common.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageResponse<T> {
    private List<T> items;
    private String nextCursor;
    private boolean hasMore;

    public PageResponse() {
    }

    public PageResponse(List<T> items, String nextCursor, boolean hasMore) {
        this.items = items;
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
    }
}
