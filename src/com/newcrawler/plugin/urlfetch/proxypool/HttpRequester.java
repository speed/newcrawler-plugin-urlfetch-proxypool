package com.newcrawler.plugin.urlfetch.proxypool;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsPSMDetector;


/**
 * HTTP请求对象
 * 
 */
public final class HttpRequester {
	private final static Log logger = LogFactory.getLog(CrawlerCookieManager.class);
	
	private static final String DEFAULT_CHARSET="UTF-8";
	private static enum methods{
		GET,
		POST
	}

	/**
	 * 发送GET请求
	 * 
	 * @param urlString URL地址
	 * @param params 参数集合
	 * @param propertys 请求属性
	 * @return 响应对象
	 * @throws IOException
	 */
	public static final HttpResponse sendGet(RequestBo requestBo)throws IOException {
		requestBo.setMethod(methods.GET.name());
		return send(requestBo);
	}

	/**
	 * 发送POST请求
	 * 
	 * @param urlString URL地址
	 * @param params 参数集合
	 * @param propertys 请求属性
	 * @return 响应对象
	 * @throws IOException
	 */
	public static final HttpResponse sendPost(RequestBo requestBo)throws IOException {
		requestBo.setMethod(methods.POST.name());
		return send(requestBo);
	}
	
	private static final String getParams(String contentEncoding, Map<String, String[]> parameters, String params) throws UnsupportedEncodingException{
		if(parameters==null){
			return params;
		}
		
		if(params==null){
			params="";
		}
		for (String key : parameters.keySet()) {
			String values[]=parameters.get(key);
			if(values!=null){
				for(String v:values){
					if(!"".equals(params)){
						params+="&";
					}
					params+=key;
					params+="=";
					String temp=URLEncoder.encode(v, contentEncoding);
					temp=temp.replaceAll("\\+",  "%20");
					params+=temp;
				}
			}
		}
		return params;
	}
	
	private static final String getParams(String urlString, String contentEncoding, Map<String, String[]> parameters, String params) throws UnsupportedEncodingException{
		List<String> paramList=new ArrayList<String>();
		urlString=URLUtil.getUrlParams(paramList, urlString);
		
		/*int invalidChar=urlString.indexOf("#");
		if(invalidChar!=-1){
			urlString=urlString.substring(0, invalidChar);
		}*/
		
		if(!paramList.isEmpty()){
			String  param = "";
			for (String temp : paramList) {
				if(temp.indexOf("=")!=-1){
					String args[]=temp.split("=", 2);
					if(!"".equals(param)){
						param+="&";
					}
					param+=args[0];
					param+="=";
					String v=URLEncoder.encode(URLDecoder.decode(args[1], contentEncoding), contentEncoding);
					v=v.replaceAll("\\+",  "%20");
					param+=v;
				}else{
					if(!"".equals(param)){
						param+="&";
					}
					param+=temp;
				}
			}
			urlString +="?" + param;
		}
		if(params==null){
			params="";
		}
		if(parameters!=null){
			for (String key : parameters.keySet()) {
				String values[]=parameters.get(key);
				if(values!=null){
					for(String v:values){
						if(!"".equals(params)){
							params+="&";
						}
						params+=key;
						params+="=";
						String temp=URLEncoder.encode(v, contentEncoding);
						temp=temp.replaceAll("\\+",  "%20");
						params+=temp;
					}
				}
			}
		}
		return params;
	}
	/**
	 * 发送HTTP请求
	 * 
	 * @param urlString
	 * @return 响映对象
	 * @throws IOException
	 */
	public static final HttpResponse send(RequestBo requestBo) throws IOException {
		String urlString=requestBo.getUrlString();
		String method=requestBo.getMethod(); 
		Map<String, String[]> parameters=requestBo.getParameters(); 
		String params=requestBo.getParams(); 
		Map<String, String> propertys=requestBo.getPropertys(); 
		
		URL urlObj = new URL(urlString);
		
		List<HttpCookie> cookieList = requestBo.getCookieList();
		if(cookieList!=null){
			SessionUtils.addCookie(requestBo.getCookieStore(), cookieList);
		}
		CrawlerCookieManager cookieManager=new CrawlerCookieManager(requestBo.getCookieStore(), null);
		CookieHandler.setDefault(cookieManager);
		
		if(propertys==null){
			propertys=new HashMap<String, String>();
		}
		
		if (method.equalsIgnoreCase(methods.GET.name())) {
			//GET
			params=getParams(DEFAULT_CHARSET, parameters, params);
			if(params!=null && !"".equals(params.trim())){
				if(urlString.indexOf("?")==-1){
					urlString +="?" + params;
				}else{
					urlString +="&" + params;
				}
			}
		}else{
			//POST
			params=getParams(urlString, DEFAULT_CHARSET, parameters, params);
		}
		
		// 设置相关属性，具体含义请查阅JDK文档
		HttpURLConnection urlConnection = initConn(requestBo.getProxy(), urlObj, propertys, requestBo.getConnectTimeout(), requestBo.getReadTimeout());
		//如果使用自动重定向HttpURLConnection不会自动绑定第一次请求的COOKIE
		urlConnection.setRequestMethod(method);
		
		// 如果请求为POST方法，并且参数不为空
		if (method.equalsIgnoreCase(methods.POST.name())) {
			// 将参数信息发送到HTTP服务器
			// 要注意：一旦使用了urlConnection.getOutputStream().write()方法，urlConnection.setRequestMethod("GET");将失效，其请求方法会自动转为POST
			urlConnection.getOutputStream().write(params.toString().getBytes());
			urlConnection.getOutputStream().flush();
			urlConnection.getOutputStream().close();
		}
		Set<String> sessionList=new HashSet<String>();
		sessionList.add(urlString);
		
		HttpResponse httpResponse = new HttpResponse();
		urlConnection=followRedirects(requestBo, httpResponse, urlConnection, requestBo.getProxy(), propertys, sessionList, 
				0, requestBo.getConnectTimeout(), requestBo.getReadTimeout(), requestBo.getRedirectsTimes());
		
		List<HttpCookie> newCookieList=requestBo.getCookieStore().getCookies();
		if(newCookieList!=null){
			List<HttpCookie> cookies=new ArrayList<HttpCookie>();
			cookies.addAll(newCookieList);
			httpResponse.setCookieList(cookies);
		}
		
		try {
			makeContent(httpResponse, urlString, requestBo.getContentEncoding(), urlConnection);
			httpResponse.setHeaders(propertys);
		} catch (IOException e) {
			throw e;
		} finally {
			// 最终关闭流
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		return httpResponse;
	}

	private static HttpURLConnection initConn(Proxy proxy, URL urlObj, Map<String,String> propertys, int connectTimeout, int readTimeout) throws IOException{
		HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection(proxy);
		
		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		urlConnection.setDefaultUseCaches(false);
		urlConnection.setUseCaches(false);
		urlConnection.setConnectTimeout(connectTimeout);
		urlConnection.setReadTimeout(readTimeout);
		urlConnection.setInstanceFollowRedirects(false);
		
		propertys.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		propertys.put("Accept-Encoding", "gzip,deflate,sdch");
		propertys.put("Connection", "Keep-Alive");//在完成本次请求的响应后，断开连接，不要等待本次连接的后续请求了
		
		// 赋予请求属性
		if (!propertys.isEmpty()){
			for (String key : propertys.keySet()) {
				String prop=propertys.get(key);
				if(prop!=null && !"".equals(prop.trim())){
					urlConnection.setRequestProperty(key, prop);
				}
			}
		}
		return urlConnection;
	}
	private static HttpURLConnection followRedirects(RequestBo requestBo, HttpResponse httpResponse, HttpURLConnection urlConnection, Proxy proxy, Map<String,String> propertys, Set<String> sessionList, 
			int times, final int connectTimeout, int readTimeout, final int redirectsTimes) throws IOException {  
		int statusCode = urlConnection.getResponseCode();  
	    String locationHeader = urlConnection.getHeaderField("Location");
	    String realURL=urlConnection.getURL().toString();
	    
	    if(!isRedirected(statusCode, locationHeader)){
	    	return urlConnection;
	    }
	    if(times>redirectsTimes){
	    	logger.error("URL redirects too many times.");
	    	return urlConnection;
	    }
	    
	    if(locationHeader==null){
	    	logger.error("Redirects url not found.");
	    	return urlConnection;
	    }
	    urlConnection.disconnect();
	    
	    String urlString=URLUtil.makeAbsoluteURL(realURL, locationHeader);
	    
		propertys.put("Referer", realURL);
		
		if(!sessionList.contains(urlString)){
			sessionList.add(urlString);
		}
		URL urlObj = new URL(urlString);
		urlConnection = initConn(proxy, urlObj, propertys, connectTimeout, readTimeout);
		urlConnection.setRequestMethod("GET");
    	
    	return followRedirects(requestBo, httpResponse, urlConnection, proxy, propertys, sessionList, ++times, connectTimeout, readTimeout, redirectsTimes);
	}
	private static boolean isRedirected(int statusCode, String locationHeader) throws IOException {  
        switch(statusCode){
	        case 302: 
	            return locationHeader != null;
	        case 301: 
	        case 307: 
	        case 303: 
	            return true;
	        case 304: 
	        case 305: 
	        case 306: 
	        default:
	            return false;
        } 
    }
	public static final HttpResponse sendFile(RequestBo requestBo) throws IOException {
		String urlString=requestBo.getUrlString();
		String receiveEncoding=requestBo.getContentEncoding();
		Map<String, String[]> parameters=requestBo.getParameters();
		Map<String,String> propertys=requestBo.getPropertys();
		Map<String, String[]> fileParameters=requestBo.getFileParameters();
		
		HttpURLConnection urlConnection = null;
		final String BOUNDARY = "---------------------------7dc17d1d2082e";
		StringBuffer param = new StringBuffer();
		if (parameters != null){
			for (String key : parameters.keySet()) {
				String[] temp=parameters.get(key);
				for(String t:temp){
					param.append("--");
					param.append(BOUNDARY);
					param.append("\r\n");
					param.append("Content-Disposition: form-data; name=\""+ key + "\" \r\n\r\n");
					param.append(t);
					param.append("\r\n");
				}
			}
		}
		
		if (fileParameters != null){
			for (String key : fileParameters.keySet()) {
				String[] temp=fileParameters.get(key);
				for(String fileUrl:temp){
					if(fileUrl==null || "".equals(fileUrl)){
						continue;
					}
					byte[] fileByte = null;
					try{
						URL url = new URL(fileUrl);
						HttpURLConnection fileConnection = (HttpURLConnection) url.openConnection(requestBo.getProxy());
						fileConnection.setConnectTimeout(requestBo.getConnectTimeout());
						fileConnection.setReadTimeout(requestBo.getReadTimeout());
						if (propertys != null){
							for (String propertysKey : propertys.keySet()) {
								fileConnection.setRequestProperty(propertysKey, propertys.get(propertysKey));
							}
						}
						InputStream is = fileConnection.getInputStream();
						fileByte = readByte(is);
						fileUrl=fileConnection.getURL().toString();
					}catch(Exception e){
						logger.error("send file", e);
						continue;
					}
					int s = fileUrl.lastIndexOf("/");
					int e = fileUrl.indexOf('?', s);
					if(e==-1){
						e= fileUrl.length();
					}
					String filename= fileUrl.substring(s + 1,e);
					param.append("--");
					param.append(BOUNDARY);
					param.append("\r\n");
					param.append("Content-Disposition: form-data; name=\""+key+"\"; filename=\""+ filename + "\"\r\n");
					param.append("Content-Type: application/x-zip-compressed\r\n\r\n");
					param.append(fileByte);
				}
			}
		}
		param.append("\r\n--" + BOUNDARY + "--\r\n");
		
		byte[] data = param.toString().getBytes();
		int contentLength=data.length;
		
		URL url = new URL(urlString);
		urlConnection = (HttpURLConnection) url.openConnection(requestBo.getProxy());
		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		urlConnection.setDefaultUseCaches(false);
		urlConnection.setUseCaches(false);
		urlConnection.setConnectTimeout(requestBo.getConnectTimeout());
		urlConnection.setReadTimeout(requestBo.getReadTimeout());
		
		urlConnection.setRequestMethod("POST");
		urlConnection.setRequestProperty("Content-Type","multipart/form-data; boundary=" + BOUNDARY); // 设置表单类型和分隔符
		urlConnection.setRequestProperty("Content-Length", String.valueOf(contentLength)); // 设置内容长度
		if (propertys != null){
			for (String key : propertys.keySet()) {
				urlConnection.setRequestProperty(key, propertys.get(key));
			}
		}
		urlConnection.getOutputStream().write(data);
		urlConnection.getOutputStream().flush();
		urlConnection.getOutputStream().close();
		
		HttpResponse httpResponse = new HttpResponse();
		List<HttpCookie> newCookieList=new ArrayList<HttpCookie>();
		httpResponse.setCookieList(newCookieList);
		
		try {
			makeContent(httpResponse, urlString, receiveEncoding, urlConnection);
		} catch (IOException e) {
			throw e;
		} finally {
			// 最终关闭流
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		
		return httpResponse;
	}
	
	/**
	 * 得到响应对象
	 * 
	 * @param urlConnection
	 * @return 响应对象
	 * @throws IOException
	 */
	private static final void makeContent(HttpResponse httpResponse, String urlString, String contentEncoding, HttpURLConnection urlConnection) throws IOException {
		final String realURL=urlConnection.getURL().toString();
		httpResponse.setRealURL(realURL);
		httpResponse.setResponseCode(urlConnection.getResponseCode());
		byte[] htmlByte =null;
		
		String encoding = urlConnection.getContentEncoding();
		
		if(urlConnection.getResponseCode()==404){
			httpResponse.setContentEncoding(contentEncoding);
			httpResponse.setContent("");
			return;
		}
		// 得到响应流
		InputStream inputStream = urlConnection.getInputStream();
		InputStream gzipInputStream=inputStream;
		try{
			if(encoding!=null && encoding.indexOf("gzip")!=-1){
				gzipInputStream = new GZIPInputStream(inputStream);
			}
		}catch(Exception e){
			logger.warn(e.getMessage());
		}
		String content=null;
		if(contentEncoding==null){
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			contentEncoding=htmlCharsetDetector(urlConnection, byteArrayOutputStream, gzipInputStream);
			htmlByte = byteArrayOutputStream.toByteArray();
			byteArrayOutputStream.close(); 
			content=new String(htmlByte, contentEncoding);
		}else{
			htmlByte = readByte(gzipInputStream);
			content=new String(htmlByte, contentEncoding);
		}
		httpResponse.setContentEncoding(contentEncoding);
        httpResponse.setLength(htmlByte.length);
		httpResponse.setContent(content);
		
		gzipInputStream.close();
	}
	
    
	@SuppressWarnings("unused")
	private static final void readByBufferedReader(InputStream inStream,String receiveEncoding, HttpResponse httpResponse) throws IOException{
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inStream,receiveEncoding));
		StringBuffer temp = new StringBuffer("");
		String line = bufferedReader.readLine();
		while (line != null) {
			temp.append(line);
			line = bufferedReader.readLine();
			if(line != null)
				temp.append("\r\n");
		}
		bufferedReader.close();
		httpResponse.setContent(temp.toString());
		httpResponse.setLength(httpResponse.getContent().length());
	}
	

	@SuppressWarnings("unused")
	private final static byte[] readByte(InputStream inputStream, Integer dataLen) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		int total=0;
		int size;
		while ((size=inputStream.read(buffer)) != -1) {
			if((total+size)>dataLen){
				size=dataLen-total;
				byteArrayOutputStream.write(buffer, 0, size);
				break;
			}
			byteArrayOutputStream.write(buffer, 0, size);
			total+=size;
		}
		byte[] dataByte = byteArrayOutputStream.toByteArray();
		byteArrayOutputStream.close(); 
		return dataByte;
	}
	
	private final static byte[] readByte(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[2048];
		int size;
		while ((size=inputStream.read(buffer)) != -1) {
			byteArrayOutputStream.write(buffer, 0, size);
		}
		byte[] dataByte = byteArrayOutputStream.toByteArray();
		byteArrayOutputStream.close(); 
		return dataByte;
	}
	
	public static final String htmlCharsetDetector(Proxy proxy, String urlString, int timeout) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(proxy);
		urlConnection.setConnectTimeout(timeout);
		urlConnection.setReadTimeout(timeout);
		return htmlCharsetDetector(urlConnection, null, urlConnection.getInputStream());
	}
	private static final String htmlCharsetDetector(HttpURLConnection urlConnection, ByteArrayOutputStream byteArrayOutputStream, InputStream inputStream) throws IOException {
		nsDetector det = new nsDetector(nsPSMDetector.ALL);
		BufferedInputStream imp = new BufferedInputStream(inputStream);
		byte[] buf = new byte[2048];
		int len;
		boolean done = false;
		boolean isAscii = true;
		while ((len = imp.read(buf, 0, buf.length)) != -1) {
			if (isAscii)
				isAscii = det.isAscii(buf, len);
			if (!isAscii && !done){
				done = det.DoIt(buf, len, false);
			}
			if(byteArrayOutputStream==null){
				break;
			}
			byteArrayOutputStream.write(buf, 0, len);
		}
		det.DataEnd();
		if(det.getProbableCharsets()!=null&&det.getProbableCharsets().length>0){
			return det.getProbableCharsets()[0];
		}
		
		String contentType=urlConnection.getContentType();
		if(contentType!=null && !"".equals(contentType)){
			//"Content-Type:text/html; charset=UTF-8", "Content-type:text/plain"
			Pattern p = Pattern.compile("charset=([a-zA-Z0-9-]*)",Pattern.DOTALL);
			Matcher m = p.matcher(contentType);
			if(m.find()) {
				return (m.group(1));
			}
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private static final HashMap<String, String> readCookie(HttpURLConnection urlConnection){
		Map<String, List<String>> map=urlConnection.getHeaderFields();
		HashMap<String,String> cookiesMap=new HashMap<String, String>();
		if(map!=null&&map.containsKey("Set-Cookie")){
			List<String> list=map.get("Set-Cookie");
			for(Iterator<String> it=list.iterator();it.hasNext();){
				String c=it.next().toString();
				String[] tempField=c.split(";\\s*");
				for(String field:tempField){
					if ("secure".equalsIgnoreCase(field)) {
						continue;
			        }
					String[] propertys=field.split("=", 2);
					if(propertys.length==2){
						String key=propertys[0];
						String value=propertys[1];
						if(!key.equalsIgnoreCase("expires")
								&& !key.equalsIgnoreCase("Path")
								&& !key.equalsIgnoreCase("domain")
								&& !cookiesMap.containsKey(key)){
							cookiesMap.put(key, value);
						}
					}
				}
			}
		}
		return cookiesMap;
	}
}
