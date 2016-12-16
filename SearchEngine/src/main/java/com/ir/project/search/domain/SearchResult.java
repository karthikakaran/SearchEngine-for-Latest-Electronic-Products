package com.ir.project.search.domain;

public class SearchResult {
	private String url;
	private String title;
	private String data;

	public SearchResult() {

	}

	public SearchResult(String url, String title, String data) {
		this.url = url;
		this.title = title;
		this.data = data;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
