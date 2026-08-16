package com.solehealth.user.common.response;

import lombok.Data;

@Data
public class PageQuery {

	private int page = 0;
	private Integer size = null; // null => use default from properties
	private Integer pageSize = null; // alias for frontend query params
	private String sort; // e.g. "username,asc" or "createDate,desc"
    private String keyword;

}
