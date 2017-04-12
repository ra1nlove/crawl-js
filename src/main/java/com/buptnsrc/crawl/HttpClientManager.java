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
        httpclient = HttpClients.custom().setConnectionManager(cm).setRetryHandler(getRetryTime()).build() ;
        return httpclient ;
    }

    /**
     * 进行超时处理
     * @return  超时处理方案的实例
     */
    private static HttpRequestRetryHandler getRetryTime() {
        HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {
            //自定义回复策略
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                //设置回复策略，当发生异常时候将自动重试3次
                if ( executionCount >3 ) {
                    return false ;
                }
                //服务停掉重新尝试连接
                if ( exception instanceof NoHttpResponseException ) {
                    return true ;
                }
                //超时
                if ( exception instanceof InterruptedIOException ) {
                    return true ;
                }
                //SSL异常不需要重试
                if ( exception instanceof SSLHandshakeException ) {
                    return false ;
                }
                if ( exception instanceof ConnectTimeoutException ) {
                    // 连接被拒
                    return false ;
                }

                HttpClientContext clientContext = HttpClientContext.adapt(context) ;
                HttpRequest request = clientContext.getRequest() ;
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest) ;
                if ( idempotent ) {
                    return true ;
                }
                return false ;
            }
        } ;
        return requestRetryHandler ;
    }

}