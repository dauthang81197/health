package com.solehealth.user.common.response;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageRequestUtils {

	private PageRequestUtils() {}

    public static Pageable of(int page, int size, String sort) {
        if (size <= 0) size = 20;
        if (page < 0) page = 0;
        Sort s = Sort.unsorted();
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            if (parts.length == 2) {
                String property = parts[0].trim();
                String dir = parts[1].trim();
                if (!property.isEmpty()) {
                    s = "desc".equalsIgnoreCase(dir) ? Sort.by(property).descending() : Sort.by(property).ascending();
                }
            } else {
                // Pattern không hợp lệ -> bỏ sort để tránh lỗi 500
                s = Sort.unsorted();
            }
        }
        return PageRequest.of(page, size, s);
    }
}


