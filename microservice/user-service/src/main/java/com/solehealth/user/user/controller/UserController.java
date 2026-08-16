package com.solehealth.user.user.controller;

import com.solehealth.user.common.response.ApiResponse;
import com.solehealth.user.common.response.PageQuery;
import com.solehealth.user.common.response.PagedResponse;
import com.solehealth.user.common.response.PagingHelper;
import com.solehealth.user.user.dto.response.UserResponse;
import com.solehealth.user.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private final PagingHelper pagingHelper;
    private final UserService userService;


    @GetMapping()
    public ApiResponse<PagedResponse<UserResponse>> list(@ModelAttribute PageQuery query) {
        Page<UserResponse> page = null;
        return ApiResponse.ok(pagingHelper.toPagedResponse(page));
    }
}