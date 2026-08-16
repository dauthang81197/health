package com.solehealth.user.common.response;

import org.springframework.data.domain.Page;

import java.util.List;

public class PagedResponse<T> {

	private List<T> content;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;

	public static <T> PagedResponse<T> from(Page<T> page) {
		PagedResponse<T> pr = new PagedResponse<>();
		pr.content = page.getContent();
		pr.page = page.getNumber();
		pr.size = page.getSize();
		pr.totalElements = page.getTotalElements();
		pr.totalPages = page.getTotalPages();
		return pr;
	}

	public List<T> getContent() { return content; }
	public void setContent(List<T> content) { this.content = content; }
	public int getPage() { return page; }
	public void setPage(int page) { this.page = page; }
	public int getSize() { return size; }
	public void setSize(int size) { this.size = size; }
	public long getTotalElements() { return totalElements; }
	public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
	public int getTotalPages() { return totalPages; }
	public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}




