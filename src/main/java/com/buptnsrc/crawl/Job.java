package com.buptnsrc.crawl;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by rain on 17-4-12.
 */
public class Job {

    BloomFilter<byte[]> bloomFilter  = BloomFilter.create(Funnels.byteArrayFunnel(),1000000);

    public Queue<String> pages = new ConcurrentLinkedDeque<>();

    String outfile = null;
    int total = 0;
    int num = 0;

    public Job(String infilePath,String outfilePath ,int num) throws IOException {

        this.outfile = outfilePath;
        this.total = num;

        File file = new File(infilePath);

        if(file.isFile() && file.exists()) {
            InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file));//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while ((lineTxt = bufferedReader.readLine()) != null){
                pages.add(lineTxt);
            }
        }
    }


    public void run(){
        PageTask pageTask = new PageTask();
        pageTask.run();
    }

    class PageTask extends Thread{
        @Override
        public void run(){
            while(true){
                try {
                    if (num > total) break;
                    String url = pages.poll();
                    if (url == null) break;
                    String result = PageDownload.download(url);
                    if (result != null) {
                        getAllUrl(result, url);
                        List<String> jss = getJs(result, url);
                        for(String js : jss){
                            String content = PageDownload.download(js);
                            String[] names = js.split("/");
                            String filename = names[names.length - 1];
                            File out = new File(outfile + filename);
                            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out)), true);
                            printWriter.write(content);
                            printWriter.close();
                            num++;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public List<String> getJs(String text,String baseurl) throws Exception{
        String jstext = "";
        List<String> result = new ArrayList<String>();
        Document doc = Jsoup.parse(text);
        Elements links = doc.select("script");
        for(Element link : links){
            String newlink = link.attr("src");
            try{
                if(newlink.length()>1 && newlink.endsWith("js")) {
                    URL url = new URL(baseurl);
                    URL newurl = new URL(url, newlink);
                    if(bloomFilter.mightContain(newurl.toString().getBytes())){
                        continue;
                    }else {
                        bloomFilter.put(newurl.toString().getBytes());
                        result.add(newurl.toString());
                    }
                }
            }catch (Exception e){
            }
            if(link.html()!=null && link.html().length()>0){
                jstext += link.html()+"\n  ============================================================ \n";
            }
            File out = new File(outfile+baseurl.replace("/","-"));
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out)), true);
            printWriter.write(jstext);
            printWriter.close();

        }

        return result;
    }

    public void getAllUrl(String text,String baseurl){
        Document doc = Jsoup.parse(text);
        Elements links = doc.select("a");
        for(Element link : links){
            String newlink = link.attr("href");
            try{
                URL url = new URL(baseurl);
                String host = url.getHost();
                URL newurl = new URL(url,newlink);
                if (urlFilter(newurl.toString())) {
                    if(bloomFilter.mightContain(newurl.toString().getBytes())){
                        continue;
                    }else if(newurl.toString().contains(host.split("\\.")[1])){
                        bloomFilter.put(newurl.toString().getBytes());
                        pages.add(newurl.toString().replaceAll("\\s", ""));
                    }
                }
            }catch (Exception ea){
            }
        }
    }

    public  boolean urlFilter(String url){
        if(url.endsWith(".js")||url.endsWith(".apk")||!url.startsWith("http")||url.endsWith("zip")||url ==null ||url.contains("comment")
                ||url.contains("javascrip")||url.contains("jpg")||url.endsWith("exe") || url.endsWith("jpg") || url.contains("#")){
            return false;
        }
        return true;
    }

    public static void main(String[] args){
        try {
            Job job = new Job(args[0], args[1], Integer.valueOf(args[2]));
            job.run();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
