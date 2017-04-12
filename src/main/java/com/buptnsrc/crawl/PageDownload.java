package com.buptnsrc.crawl;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Created by rain on 17-2-24.
 */
public class PageDownload {

    static CloseableHttpClient httpclient= HttpClientManager.getHttpClient();
    private static String[] proxys = {"proxy2.asec.buptnsrc.com","proxy.asec.buptnsrc.com","proxy1.asec.buptnsrc.com"};

    public static String download(String url){
        String result = null;
        HttpGet httpget = null;
        int statuscode =0;
        for(String proxy : proxys){
            try {
                httpget = new HttpGet(url);
                HttpHost host = new HttpHost(proxy, 8001, "http");
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(10000)
                        .setConnectionRequestTimeout(3000)
                        .setSocketTimeout(10000)
                        .setProxy(host)
                        .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                        .build();
                //设置请求头
                httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.79 Safari/537.1");
                httpget.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                httpget.setHeader("Accept-Encoding", "gzip, deflate, sdch");
                httpget.setConfig(requestConfig);

                CloseableHttpResponse resp = httpclient.execute(httpget);
                statuscode = resp.getStatusLine().getStatusCode();
                Header[] header = resp.getHeaders("Content-Type");

                if(statuscode==200){
                    HttpEntity entity = resp.getEntity();
                    result = EntityUtils.toString(entity);
                    System.out.println("download success "+url);
                    return result;
                }else {
                    System.out.println("download fail statuscode "+statuscode +" : "+url);
                }
            }catch (Exception e ){
                e.printStackTrace();
            }finally {
                httpget.abort();
            }
        }
        return result;
    }



}
