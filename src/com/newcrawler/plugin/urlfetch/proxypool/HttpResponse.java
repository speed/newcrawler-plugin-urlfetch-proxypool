package com.newcrawler.plugin.urlfetch.proxypool;

import java.util.List;
import java.util.Map;

/**
 * 响应对象
 */
public class HttpResponse {

	private Map<String, String> cookies;
	private int length;
	private String content;// 内容
	private String realURL;
	private String contentEncoding;
	private int responseCode;
	private Map<String, Map<String, List<String>>> headerMap;
	public Map<String, String> getCookies() {
		return cookies;
	}
	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getRealURL() {
		return realURL;
	}
	public void setRealURL(String realURL) {
		this.realURL = realURL;
	}
	public String getContentEncoding() {
		return contentEncoding;
	}
	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public Map<String, Map<String, List<String>>> getHeaderMap() {
		return headerMap;
	}
	public void setHeaderMap(Map<String, Map<String, List<String>>> headerMap) {
		this.headerMap = headerMap;
	}
}
