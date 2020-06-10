package com.newcrawler.plugin.urlfetch.proxypool;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class URLUtil {
	private final static Log logger = LogFactory.getLog(URLUtil.class);
	/**
	 * @param absoluteURL 绝对路径
	 * @param innerURL 相对路径
	 * @return
	 */
	public static String makeAbsoluteURL(String absoluteURL, String innerURL) {
		try {
			return URLUtil.makeAbsoluteURL(new URL(absoluteURL), innerURL);
		} catch (MalformedURLException e) {
			logger.error("", e);
			return null;
		}
	}
	/**
	 * 相对路径转绝对路径
	 * @param absoluteURL
	 * @param innerURL
	 * @return
	 */
	public static String makeAbsoluteURL(URL absoluteURL, String innerURL) {
		if (innerURL==null) {
			innerURL="";
		}
		try {
			if (innerURL.toLowerCase().startsWith("http://")) {
				URL url = new URL(innerURL);
				String path=url.getPath();
				if(path==null || "".equals(path.trim())){
					path="/";
				}
				String query=url.getQuery();
				if(query!=null && !"".equals(query.trim())){
					path+="?"+query;
				}
				url = new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
				return url.toString();
			}
			if (innerURL.startsWith("?")) {
				innerURL=absoluteURL.getPath()+innerURL;
			}
			
			URL url = new URL(absoluteURL, innerURL);
			return url.toString();
		} catch (MalformedURLException e) {
			return null;
		}
	}
	public static List<String> getUrlParams(String urlString){
		List<String> params=new ArrayList<String>();
		getUrlParams(params,urlString);
		return params;
	}
	public static String getUrlParams(List<String> params, String urlString){
		//xxx.xxx.xx/xxx.jsp?p=1&p=2
		int i=urlString.indexOf("?");
		if(i<0){
			return urlString;
		}
		String temp=urlString.substring(i+1);
		String paramsStr[]=temp.split("\\&");
		for(String param:paramsStr){
			params.add(param);
		}
		return urlString.substring(0,i);
	}
	public static String getBaseURL(String absoluteURL) {
		if(absoluteURL.toUpperCase().startsWith("HTTP://")){
			String regex="http://.*?/";
			Pattern pattern = Pattern.compile(regex,Pattern.DOTALL);
			Matcher matcher = pattern.matcher(absoluteURL);
			while(matcher.find()) {
				return matcher.group();
			}
		}
		return null;
	}
	
}
