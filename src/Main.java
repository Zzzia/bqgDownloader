import engine.FastDownloader;
import source.Bishenge;
import source.E8;

import java.io.IOException;

/**
 * Created By zia on 2018/10/5.
 * 写了四个网站的解析，可能以后会添加更多
 * 使用IFastDownloader抽象类进行统一调用
 * 可以通过实现抽象类快速添加新的网站源解析
 */
public class Main {

    //这两个参数请自行更换
    private final static String bookName = "赤心巡天";
    //存放目录，该目录是mac系统下的，windows需要自行更正“\\”
    private final static String savePath = ".";

    public static void main(String[] args) throws IOException, InterruptedException {

        FastDownloader downloader = new Bishenge(bookName, "https://www.xbiquge.bz/book/54531/", savePath);

        //下载全部内容到一个txt文件里
        downloader.downloadTXT();
        //下载epub格式，自动生成索引
//        downloader.downloadEPUB();
        //下载epub并转换为mobi格式
        //downloader.downloadMOBI();
    }
}
