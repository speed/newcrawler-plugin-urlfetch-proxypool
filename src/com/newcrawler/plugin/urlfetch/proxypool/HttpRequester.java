package com.newcrawler.plugin.urlfetch.proxypool;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsPSMDetector;



/**
 * HTTP请求对象
 * 
 * @author YaoMing
 */
public final class HttpRequester {
	private static final Log log = LogFactory.getLog(HttpRequester.class);
	private static final String DEFAULT_CHARSET="UTF-8";
	private static final int TIMEOUT=45000;// GAE限制最大请求延迟60秒
	private static final int MAX_REDIRECTS=10;// 最大的重定向次数
	private static enum methods{
		GET,
		POST
	}
	
	public static final HttpResponse sendGet(final String urlString, Proxy proxy) throws IOException {
		return sendGet(urlString, null, null, null, proxy);
	}

	/**
	 * 发送GET请求
	 * 
	 * @param urlString URL地址
	 * @param params 参数集合
	 * @return 响应对象
	 * @throws IOException
	 */
	public static final HttpResponse sendGet(String urlString, String contentEncoding, Proxy proxy)
			throws IOException {
		return sendGet(urlString, contentEncoding, null, null, proxy);
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
	public static final HttpResponse sendGet(String urlString, String contentEncoding, Map<String, String> propertys, String cookieString, Proxy proxy)throws IOException {
		return send(urlString, methods.GET.name(), contentEncoding, null, null, propertys, cookieString, proxy);
	}

	/**
	 * 发送POST请求
	 * 
	 * @param urlString
	 * @return
	 * @throws IOException
	 */
	public static HttpResponse sendPost(final String urlString, Proxy proxy) throws IOException {
		return sendPost(urlString, null, null, null, null, null, proxy);
	}
	/**
	 * 发送POST请求
	 * 
	 * @param urlString URL地址
	 * @return 响应对象
	 * @throws IOException
	 */
	public static final HttpResponse sendPost(String urlString, String contentEncoding, Proxy proxy) throws IOException {
		return sendPost(urlString, contentEncoding, null, null, null, null, proxy);
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
	public static final HttpResponse sendPost(String urlString, String contentEncoding, Map<String, String[]> parameters, String params, Map<String, String> propertys, String cookieString, Proxy proxy)throws IOException {
		return send(urlString, methods.POST.name(), contentEncoding, parameters, params, propertys, cookieString, proxy);
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
	public static final HttpResponse send(String urlString, String method, String contentEncoding, Map<String, String[]> parameters, String params, Map<String,String> propertys, String cookieString, Proxy proxy) throws IOException {
		if(contentEncoding==null || "".equals(contentEncoding)){
			contentEncoding=DEFAULT_CHARSET;
		}
		if(propertys==null){
			propertys=new HashMap<String, String>();
		}
		
		if (method.equalsIgnoreCase(methods.GET.name())) {
			//GET
			params=getParams(contentEncoding, parameters, params);
			if(StringUtils.isNotBlank(params)){
				if(urlString.indexOf("?")==-1){
					urlString +="?" + params;
				}else{
					urlString +="&" + params;
				}
			}
		}else{
			//POST
			params=getParams(urlString, contentEncoding, parameters, params);
		}
		Map<String, Map<String, List<String>>> headerMap=new HashMap<String, Map<String, List<String>>>();
		// 设置相关属性，具体含义请查阅JDK文档
		HttpURLConnection urlConnection = initConn(urlString, propertys, cookieString, headerMap, proxy);
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
		urlConnection=followRedirects(urlConnection, propertys, cookieString, headerMap, proxy, 0);
		
		HttpResponse hr=null;
		try {
			hr= makeContent(urlString, contentEncoding, urlConnection);
		} catch (IOException e) {
			throw e;
		} finally {
			// 最终关闭流
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		return hr;
	}
	

	private static HttpURLConnection initConn(String urlString, Map<String,String> propertys, String cookieString, Map<String, Map<String, List<String>>> headerMap, Proxy proxy) throws IOException{
		URL url = new URL(urlString);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(proxy);
		
		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		urlConnection.setDefaultUseCaches(false);
		urlConnection.setUseCaches(false);
		urlConnection.setConnectTimeout(TIMEOUT);
		urlConnection.setReadTimeout(TIMEOUT);
		
		urlConnection.setInstanceFollowRedirects(false);
		// 赋予请求属性
		if (!propertys.isEmpty()){
			for (String key : propertys.keySet()) {
				String prop=propertys.get(key);
				if(prop!=null && !"".equals(prop.trim())){
					urlConnection.setRequestProperty(key, prop);
				}
			}
		}
		if(cookieString!=null && !"".equals(cookieString.trim())){
			urlConnection.setRequestProperty("Cookie", cookieString);
		}
		
		//通常指定压缩方法，是否支持压缩，支持什么压缩方法（gzip，deflate） 
		urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		//在完成本次请求的响应后，断开连接，不要等待本次连接的后续请求了
		urlConnection.setRequestProperty("Connection", "Keep-Alive");
		
		return urlConnection;
	}
	private static HttpURLConnection followRedirects(HttpURLConnection urlConnection, Map<String,String> propertys, String cookieString, Map<String, Map<String, List<String>>> headerMap, Proxy proxy, int times) throws IOException {  
		int statusCode = urlConnection.getResponseCode();  
		
		String realURL=urlConnection.getURL().toString();
		Map<String, List<String>> headerFields=urlConnection.getHeaderFields();
		headerMap.put(realURL, headerFields);
	    
		
	    String locationHeader = urlConnection.getHeaderField("Location");
	    
	    if(!isRedirected(statusCode, locationHeader)){
	    	return urlConnection;
	    }
	    if(times>MAX_REDIRECTS){
	    	log.error("URL redirects too many times.");
	    	return urlConnection;
	    }
	    
	    if(locationHeader==null){
	    	log.error("Redirects url not found.");
	    	return urlConnection;
	    }
	    
	    if(!locationHeader.toLowerCase().startsWith("http://") && !locationHeader.toLowerCase().startsWith("https://")){
	    	log.error("Redirects url["+locationHeader+"] errors.");
	    	return urlConnection;
	    }
	    
		urlConnection.disconnect();
		propertys.put("Referer", realURL);
		
		urlConnection = initConn(locationHeader, propertys, cookieString, headerMap, proxy);
		urlConnection.setRequestMethod("GET");
    	
    	return followRedirects(urlConnection, propertys, cookieString, headerMap, proxy, ++times);
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
	
	/**
	 * 得到响应对象
	 * 
	 * @param urlConnection
	 * @return 响应对象
	 * @throws IOException
	 */
	private static final HttpResponse makeContent(String urlString, String contentEncoding, HttpURLConnection urlConnection) throws IOException {
		int responseCode=urlConnection.getResponseCode();
		// 得到响应流
		InputStream inputStream = urlConnection.getInputStream();
		
		HttpResponse httpResponser = new HttpResponse();
		final String realURL=urlConnection.getURL().toString();
		httpResponser.setRealURL(realURL);
		httpResponser.setContentEncoding(contentEncoding);
		httpResponser.setResponseCode(responseCode);
		byte[] htmlByte =null;
		
		String encoding = urlConnection.getContentEncoding();
		InputStream gzipInputStream=inputStream;
		try{
			if(encoding!=null && encoding.indexOf("gzip")!=-1){
				gzipInputStream = new GZIPInputStream(inputStream);
			}
		}catch(Exception e){
			log.warn(e);
		}
		htmlByte = readByte(gzipInputStream);
		
        httpResponser.setLength(htmlByte.length);
		httpResponser.setContent(new String(htmlByte, contentEncoding));
		inputStream.close();
		return httpResponser;
	}

	public static final String htmlCharsetDetector(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setConnectTimeout(TIMEOUT);
		urlConnection.setReadTimeout(TIMEOUT);
		return htmlCharsetDetector(urlConnection.getInputStream());
	}
	
	@SuppressWarnings("unused")
	private static final void readByBufferedReader(InputStream inStream,String receiveEncoding, HttpResponse httpResponser) throws IOException{
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
		httpResponser.setContent(temp.toString());
		httpResponser.setLength(httpResponser.getContent().length());
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
	
	private static final String htmlCharsetDetector(InputStream inputStream) throws IOException {
		nsDetector det = new nsDetector(nsPSMDetector.ALL);
		BufferedInputStream imp = new BufferedInputStream(inputStream);
		byte[] buf = new byte[128];
		int len;
		boolean done = false;
		boolean isAscii = true;
		while ((len = imp.read(buf, 0, buf.length)) != -1) {
			if (isAscii)
				isAscii = det.isAscii(buf, len);
			if (!isAscii && !done){
				done = det.DoIt(buf, len, false);
				break;
			}
		}
		det.DataEnd();
		if(det.getProbableCharsets()!=null&&det.getProbableCharsets().length>0){
			return det.getProbableCharsets()[0];
		}
		return DEFAULT_CHARSET;
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
