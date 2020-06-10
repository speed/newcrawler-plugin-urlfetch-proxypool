package com.newcrawler.plugin.urlfetch.proxypool;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionUtils {
	
	public static final void clearCookie(CookieStore cookieStore, URI uri){
		List<HttpCookie> cookieList=cookieStore.get(uri);
		if(cookieList!=null){
			for(HttpCookie httpCookie:cookieList){
				cookieStore.remove(null, httpCookie);
			}
		}
	}
	public static final void addCookie(CookieStore cookieStore, List<HttpCookie> cookieList){
		if(cookieList!=null){
			for(HttpCookie httpCookie:cookieList){
				cookieStore.add(null, httpCookie);
			}
		}
	}
	public static final void addCookie(CookieStore cookieStore, URL url, String cookies){
		List<HttpCookie> cookieList=CookieUtils.getCookies(url, cookies);
		addCookie(cookieStore, cookieList);
	}
	
	public static final void addCookie(HttpURLConnection urlConnection, List<HttpCookie> cookieList) throws URISyntaxException{
		if(cookieList!=null){
			for(HttpCookie httpCookie:cookieList){
				String cookieString=httpCookie.getName()+"="+httpCookie.getValue();
				urlConnection.addRequestProperty("Cookie", cookieString);
			}
		}
	}
	
	public static final Map<String, String> getCookie(List<HttpCookie> cookieList){
		Map<String, String> map=new HashMap<String, String>();
		if(cookieList!=null){
			for(HttpCookie httpCookie:cookieList){
				map.put(httpCookie.getName(), httpCookie.getValue());
			}
		}
		return map;
	}

}
