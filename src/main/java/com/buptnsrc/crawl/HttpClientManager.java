package com.buptnsrc.crawl;

import java.io.IOException;
import java.io.InterruptedIOException;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

public class HttpClientManager {

    //连接池管理
    private static 	PoolingHttpClientConnectionManager cm  ;

    /**
     * 对连接池进行初始化
     */
    static{
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory() ;
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory() ;
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainsf)
                .register("https", sslsf)
                .build() ;
        cm = new PoolingHttpClientConnectionManager(registry) ;
        cm.setDefaultMaxPerRoute(200) ;
        cm.setMaxTotal(300) ;
    }

    /**
     * 返回一个httpclient实例
     * @return 返回httpclient实例
     */
    public  static CloseableHttpClient getHttpClient() {
        CloseableHttpClient httpclient = null;
        httpclient = HttpClients.custom().setConnectionManager(cm).build() ;
        return httpclient ;
    }

}