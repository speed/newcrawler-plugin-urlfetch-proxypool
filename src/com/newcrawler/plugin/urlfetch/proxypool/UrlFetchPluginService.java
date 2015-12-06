package com.newcrawler.plugin.urlfetch.proxypool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.soso.plugin.UrlFetchPlugin;
import com.soso.plugin.bo.UrlFetchPluginBo;

public class UrlFetchPluginService implements UrlFetchPlugin{
	
	private final static Log logger = LogFactory.getLog(UrlFetchPluginService.class);
	public static final String REDIS_IP = "redis.ip";
	public static final String REDIS_PORT = "redis.port";
	public static final String REDIS_KEY = "redis.key";
	
	public static void main(String[] args){
		Map<String, String> properties=new HashMap<String, String>(); 
		properties.put(REDIS_IP, "127.0.0.1");
		properties.put(REDIS_PORT, String.valueOf(6379));
		properties.put(REDIS_KEY, "NC_PROXY_CACHE");
		
		String crawlUrl="http://item.jd.com/832705.html"; 
		String method=null; 
		String cookie=null; 
		String userAgent="Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.36 Safari/535.7"; 
		String encoding="GB2312";
		UrlFetchPluginService urlFetchPluginService=new UrlFetchPluginService();
		//urlFetchPluginService.execute(properties, crawlUrl, method, cookie, userAgent, encoding);
		Map<String, String> headers=new HashMap<String, String>(); 
		
		crawlUrl="http://proxy.02ta.com/header"; 
		UrlFetchPluginBo urlFetchPluginBo=new UrlFetchPluginBo(properties, headers, crawlUrl, method, cookie, userAgent, encoding);
		
		urlFetchPluginService.execute(urlFetchPluginBo);
	}
	
	@Override
	public Map<String, Object> execute(UrlFetchPluginBo urlFetchPluginBo) {
		Map<String, String> properties=urlFetchPluginBo.getProperties();
		Map<String, String> headers=urlFetchPluginBo.getHeaders();
		String crawlUrl=urlFetchPluginBo.getCrawlUrl();
		String method=urlFetchPluginBo.getMethod();
		String cookie=urlFetchPluginBo.getCookie();
		String userAgent=urlFetchPluginBo.getUserAgent();
		String encoding=urlFetchPluginBo.getEncoding();
		
		
		String cacheKey=null;
		String redisIP=null;
		int redisPort=-1;
		
		if (properties != null) {
			if (properties.containsKey(REDIS_IP) && !"".equals(properties.get(REDIS_IP))) {
				redisIP = properties.get(REDIS_IP);
			}
			
			if (properties.containsKey(REDIS_PORT) && !"".equals(properties.get(REDIS_PORT))) {
				redisPort = Integer.parseInt(properties.get(REDIS_PORT));
			}

			if (properties.containsKey(REDIS_KEY) && !"".equals(properties.get(REDIS_KEY))) {
				cacheKey = properties.get(REDIS_KEY);
			}
		}
		if(headers==null){
			headers = new HashMap<String, String>();
		}
		if(StringUtils.isNoneBlank(cookie)){
			headers.put("Cookie", cookie);
		}
		if(StringUtils.isNoneBlank(userAgent)){
			headers.put("User-Agent", userAgent);
		}
		
		Map<String, Object> map=null;
		ProxyCache proxyCache=ProxyCache.getInstance(cacheKey, redisIP, redisPort);
		for(int i=0;i<15;i++){
			//最多重试30次
			String proxyStr=proxyCache.get();
			String proxyIP=null;
			int proxyPort;
			if(proxyStr==null){
				map = new HashMap<String, Object>();
				map.put(RETURN_DATA_KEY_CONTENT, "没有可用代理IP");
				logger.error("没有可用代理IP.");
				return map;
			}
			String temp[]=proxyStr.split(":");
			proxyIP=temp[0];
			proxyPort=Integer.parseInt(temp[1]);
			if(proxyIP==null){
				proxyCache.del(proxyStr);
				continue;
			}
			try {
				map=read(proxyIP, proxyPort, headers, crawlUrl, method, encoding);
				break;
			} catch (SocketException e) {
				boolean status=proxyCache.del(proxyStr);
				logger.error("Proxy '"+proxyStr+"' error, del cache:"+status+", times:"+i+", "+e.getMessage());
				continue;
			} catch (IOException e) {
				boolean status=proxyCache.del(proxyStr);
				logger.error("Proxy '"+proxyStr+"' error, del cache:"+status+", times:"+i+", "+e.getMessage());
				continue;
			}
		}
		return map;
	}
	
	private Map<String, Object> read(String proxyIP, int proxyPort, Map<String, String> headers, String crawlUrl, String method, String encoding) throws IOException{
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIP, proxyPort)); // 实例化本地代理对象，端口为8888
		
		String cookie=null;
	    if(headers.containsKey("Cookie")){
	    	cookie=headers.get("Cookie");
	    	headers.remove("Cookie");
	    }
		HttpResponse httpResponse = HttpRequester.sendGet(crawlUrl, encoding, headers, cookie, proxy);
		
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(RETURN_DATA_KEY_COOKIES, httpResponse.getHeaderMap());
		map.put(RETURN_DATA_KEY_CONTENT, httpResponse.getContent());
		map.put(RETURN_DATA_KEY_REALURL, httpResponse.getRealURL());
		map.put(RETURN_DATA_KEY_HEADERS, httpResponse.getHeaderMap());
		return map;
	}

	@Override
	public void destory() {
		
	}
	
}
