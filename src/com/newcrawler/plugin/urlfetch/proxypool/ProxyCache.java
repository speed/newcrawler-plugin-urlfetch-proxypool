package com.newcrawler.plugin.urlfetch.proxypool;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class ProxyCache {
	private static final Log log = LogFactory.getLog(ProxyCache.class);
	
	private volatile int index=0;
	private static List<String> list=null;
	private static volatile ProxyCache proxyCache=null;
	private JedisPool pool = null;
	private String cacheKey;
	
	public static ProxyCache getInstance(String cacheKey, String redisIP, int redisPort){
		if(proxyCache==null){
			synchronized (ProxyCache.class) {
				if(proxyCache==null){
					proxyCache=new ProxyCache(cacheKey, redisIP, redisPort);
				}
			}
		}
		return proxyCache;
	}
	
	private ProxyCache(String cacheKey, String redisIP, int redisPort){
		JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(5);
        config.setMaxWaitMillis(1000 * 100);
        pool = new JedisPool(config, redisIP, redisPort);
        initList(cacheKey);
        this.cacheKey=cacheKey;
	}
	
	private synchronized void initList(String listKey) {
		Jedis conn = getConnection();
		if (conn == null) {
			return;
		}
		list = conn.lrange(listKey, 0, -1);
		//使用结束后要将jedis放回pool中
		pool.returnResource(conn);
		index=0;
	}
	
	private Jedis getConnection() {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
		} catch (Exception e) {
			log.error("Jedis get connection error.", e);
		}
		return jedis;
	}
	
	public synchronized String get(){
		if(list==null || list.isEmpty()){
			initList(cacheKey);
			if(list==null || list.isEmpty()){
				return null;
			}
		}
		int len=list.size();
		if(index>=len && index!=0){
			index=0;
		}
		String proxy=list.get(index);
		index++;
		
		return proxy;
	}
	
	public synchronized boolean del(String value){
		if(list.contains(value) && list.remove(value)){
			index--;
		}
		Jedis conn = getConnection();
		if (conn == null) {
			return false;
		}
		long count=conn.lrem(cacheKey, 1, value);
		//使用结束后要将jedis放回pool中
		pool.returnResource(conn);
		if(count>0){
			return true;
		}
		return false;
	}
}
