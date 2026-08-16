package com.solehealth.user.user.service;

import com.solehealth.user.common.response.PageQuery;
import com.solehealth.user.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;

public class UserServiceImpl implements UserService {
    @Override
    public Page<UserResponse> search(PageQuery query) {
        return null;
    }
}
