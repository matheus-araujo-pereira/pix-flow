package com.matheus.pixflow.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated response wrapper")
public record PageResponse<T>(
        @Schema(description = "List of items")
        List<T> content,

        @Schema(description = "Current page index (0-based)", example = "0")
        int page,

        @Schema(description = "Number of items per page", example = "20")
        int size,

        @Schema(description = "Total number of items across all pages", example = "105")
        long totalElements,

        @Schema(description = "Total number of pages", example = "6")
        int totalPages
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) totalElements / (double) size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }
}

