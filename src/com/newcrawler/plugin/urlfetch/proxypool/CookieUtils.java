package com.newcrawler.plugin.urlfetch.proxypool;

import java.net.HttpCookie;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CookieUtils {
	public static final List<HttpCookie> getCookies(URL url, String cookie){
		return getCookies(url, 0, cookie);
	}
	public static final List<HttpCookie> getCookies(URL url, int version, String cookie){
		if(cookie==null || "".equals(cookie)){
			return null;
		}
		if(!cookie.endsWith(";")){
			cookie=cookie+";";
		}
		List<HttpCookie> cookieList=new ArrayList<HttpCookie>();
		String cookies[]=cookie.split(";");
		for(String c:cookies){
			String temp[]=c.split("=", 2);
			HttpCookie httpCookie=new HttpCookie(temp[0], temp[1]);
			httpCookie.setDomain(url.getHost());
			httpCookie.setPath(url.getPath());
			httpCookie.setVersion(version);
			cookieList.add(httpCookie);
		}
		return cookieList;
	}
	
	public static final String getCookies(List<HttpCookie> cookieList){
		String cookie="";
		for(HttpCookie httpCookie:cookieList){
			cookie+=httpCookie.getName()+"="+httpCookie.getValue()+";";
		}
		return cookie;
	}

}
