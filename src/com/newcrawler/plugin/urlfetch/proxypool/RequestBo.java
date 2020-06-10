package com.newcrawler.plugin.urlfetch.proxypool;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.Proxy;
import java.util.List;
import java.util.Map;

public class RequestBo {
	private String urlString; 
	private String method; 
	private String contentEncoding; 
	private Map<String, String[]> parameters; 
	private String params; 
	private Map<String, String> propertys; 
	private List<HttpCookie> cookieList;
	
	private Map<String, String[]> fileParameters;
	private Proxy proxy;
	private int connectTimeout=15000;// 连接超时
	private int readTimeout=40000;// GAE限制最大请求延迟60秒
	
	private int redirectsTimes=15;// 最大的重定向次数
	
	private CookieStore cookieStore;
	
	public RequestBo(CookieStore cookieStore) {
		super();
		this.cookieStore = cookieStore;
	}
	public String getUrlString() {
		return urlString;
	}
	public void setUrlString(String urlString) {
		this.urlString = urlString;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getContentEncoding() {
		return contentEncoding;
	}
	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}
	public Map<String, String[]> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, String[]> parameters) {
		this.parameters = parameters;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}
	public Map<String, String> getPropertys() {
		return propertys;
	}
	public void setPropertys(Map<String, String> propertys) {
		this.propertys = propertys;
	}
	public List<HttpCookie> getCookieList() {
		return cookieList;
	}
	public void setCookieList(List<HttpCookie> cookieList) {
		this.cookieList = cookieList;
	}
	public int getConnectTimeout() {
		return connectTimeout;
	}
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	public int getReadTimeout() {
		return readTimeout;
	}
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout*1000;
	}
	public int getRedirectsTimes() {
		return redirectsTimes;
	}
	public void setRedirectsTimes(int redirectsTimes) {
		this.redirectsTimes = redirectsTimes;
	}
	public Map<String, String[]> getFileParameters() {
		return fileParameters;
	}
	public void setFileParameters(Map<String, String[]> fileParameters) {
		this.fileParameters = fileParameters;
	}
	public Proxy getProxy() {
		return proxy;
	}
	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}
	public CookieStore getCookieStore() {
		return cookieStore;
	}
	public void setCookieStore(CookieStore cookieStore) {
		this.cookieStore = cookieStore;
	}
}
