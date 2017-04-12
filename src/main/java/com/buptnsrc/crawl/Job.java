package com.buptnsrc.crawl;

import java.io.*;

/**
 * Created by rain on 17-4-12.
 */
public class Job {


    public void run(String infilePath,String outfilePath){
        File file = new File(infilePath);
        if(file.isFile() && file.exists()) { //判断文件是否存在
            try {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file));//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int i = 1;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    String url = lineTxt;
                    String result = PageDownload.download(url);
                    if(result!=null) {
                        File out = new File(outfilePath + i);
                        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out)), true);
                        printWriter.write(result);
                        printWriter.close();
                        i++;
                    }
                }
                read.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("找不到指定的文件");
        }

    }

    public static void main(String[] args){
        Job job = new Job();
        job.run(args[0],args[1]);
    }

}
