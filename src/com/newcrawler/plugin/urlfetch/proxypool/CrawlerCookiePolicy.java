package com.newcrawler.plugin.urlfetch.proxypool;

import java.net.HttpCookie;
import java.net.URI;

public interface CrawlerCookiePolicy {
	/**
     * One pre-defined policy which accepts all cookies.
     */
    public static final CrawlerCookiePolicy ACCEPT_ALL = new CrawlerCookiePolicy(){
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            return true;
        }
    };

    /**
     * One pre-defined policy which accepts no cookies.
     */
    public static final CrawlerCookiePolicy ACCEPT_NONE = new CrawlerCookiePolicy(){
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            return false;
        }
    };

    /**
     * One pre-defined policy which only accepts cookies from original server.
     */
    public static final CrawlerCookiePolicy ACCEPT_ORIGINAL_SERVER  = new CrawlerCookiePolicy(){
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            return HttpCookie.domainMatches(cookie.getDomain(), uri.getHost());
        }
    };


    /**
     * Will be called to see whether or not this cookie should be accepted.
     *
     * @param uri       the URI to consult accept policy with
     * @param cookie    the HttpCookie object in question
     * @return          <tt>true</tt> if this cookie should be accepted;
     *                  otherwise, <tt>false</tt>
     */
    public boolean shouldAccept(URI uri, HttpCookie cookie);
}
