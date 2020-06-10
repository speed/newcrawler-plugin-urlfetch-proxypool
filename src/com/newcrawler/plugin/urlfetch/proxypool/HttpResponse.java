package com.newcrawler.plugin.urlfetch.proxypool;

import java.io.Serializable;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

/**
 * 响应对象
 */
public class HttpResponse implements Serializable{
	private static final long serialVersionUID = 4045756729934190952L;
	private int length;
	private String content;// 内容
	private String realURL;
	private String contentEncoding;
	private int responseCode;
	private Map<String,String> headers;
	private List<HttpCookie> cookieList;
	
	private String remark;// 内容
	
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
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	public List<HttpCookie> getCookieList() {
		return cookieList;
	}
	public void setCookieList(List<HttpCookie> cookieList) {
		this.cookieList = cookieList;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}
