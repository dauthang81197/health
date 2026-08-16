package com.solehealth.user.user.service;

import com.solehealth.user.common.response.PageQuery;
import com.solehealth.user.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;

@Service
public interface UserService {
    Page<UserResponse> search(PageQuery query);
}
