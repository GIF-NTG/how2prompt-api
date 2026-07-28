package com.example.how2prompt.common.utils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public class CursorUtil {

    private static final String SEPARATOR = "|";

    public static class DecodedCursor {
        private final Object sortValue;
        private final UUID id;

        public DecodedCursor(Object sortValue, UUID id) {
            this.sortValue = sortValue;
            this.id = id;
        }

        public Object getSortValue() {
            return sortValue;
        }

        public UUID getId() {
            return id;
        }
    }

    /**
     * Mã hóa cursor gồm giá trị sắp xếp và UUID.
     */
    public static String encode(Object sortValue, UUID id) {
        if (sortValue == null || id == null) {
            return null;
        }
        String raw;
        if (sortValue instanceof Instant instant) {
            raw = instant.toString() + SEPARATOR + id;
        } else {
            raw = sortValue.toString() + SEPARATOR + id;
        }
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Giải mã cursor thành đối tượng DecodedCursor.
     * Hỗ trợ kiểu dữ liệu số (trending/usageCount) và Instant (thời gian).
     */
    public static DecodedCursor decode(String cursorStr, String sortType) {
        if (cursorStr == null || cursorStr.isBlank()) {
            return null;
        }
        try {
            String decodedRaw = new String(Base64.getDecoder().decode(cursorStr), StandardCharsets.UTF_8);
            int index = decodedRaw.indexOf(SEPARATOR);
            if (index == -1) {
                return null;
            }
            String sortValStr = decodedRaw.substring(0, index);
            String idStr = decodedRaw.substring(index + 1);
            UUID id = UUID.fromString(idStr);

            Object sortValue;
            if ("trending".equals(sortType) || "popular".equals(sortType)) {
                sortValue = Long.parseLong(sortValStr);
            } else {
                // Mặc định hoặc newest/featured dùng Instant
                sortValue = Instant.parse(sortValStr);
            }
            return new DecodedCursor(sortValue, id);
        } catch (Exception e) {
            return null; // Trả về null nếu cursor không hợp lệ
        }
    }
}
