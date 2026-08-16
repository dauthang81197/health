package com.solehealth.user.common.response;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import com.solehealth.user.common.config.PagingProperties;

@Component
public class PagingHelper {

	private final PagingProperties properties;

	public PagingHelper(PagingProperties properties) {
		this.properties = properties;
	}

	public Pageable toPageable(PageQuery query) {
		int page = Math.max(0, query.getPage());
		Integer sizeParam = query.getSize() == null ? query.getPageSize() : query.getSize();
		int size = sizeParam == null ? properties.getDefaultSize() : sizeParam;
		size = Math.max(1, Math.min(size, properties.getMaxSize()));
		return PageRequestUtils.of(page, size, query.getSort());
	}

	public <T> PagedResponse<T> toPagedResponse(Page<T> page) {
		return PagedResponse.from(page);
	}
}


