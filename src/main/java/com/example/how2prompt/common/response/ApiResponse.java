package com.example.how2prompt.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Envelope thành công chuẩn.
 *
 * <pre>{@code
 * // single / object
 * { "data": { ... } }
 *
 * // page
 * { "data": [ ... ], "meta": { "page", "size", "totalElements", ... } }
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        T data,
        PageMeta meta
) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, null);
    }

    public static <T> ApiResponse<T> empty() {
        return new ApiResponse<>(null, null);
    }

    public static <T> ApiResponse<List<T>> page(Page<T> page) {
        return new ApiResponse<>(page.getContent(), PageMeta.from(page));
    }

    public static <T, R> ApiResponse<List<R>> page(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent().stream().map(mapper).toList();
        return new ApiResponse<>(content, PageMeta.from(page));
    }

    public static <T> ApiResponse<List<T>> page(List<T> data, PageMeta meta) {
        return new ApiResponse<>(data, meta);
    }
}
