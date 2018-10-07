import source.Biquge;
import source.Bishenge;
import source.FastDownloader;
import source.Kanshenzuo;

import java.io.IOException;

/**
 * Created By zia on 2018/10/5.
 * 写了笔趣阁和笔神阁两个网站的获取方法
 * 使用IFastDownloader抽象类进行统一调用
 * 可以通过实现抽象类快速添加新的网站源解析
 *
 * 使用方法：手动找到小说目录页面，将url交给本程序处理，即可解析章节并下载
 */
public class Main {

    //这两个参数请自行更换
    private final static String bookName = "修真聊天群";
    //存放目录，实例目录是mac系统下的，windows需要自行更正“\\”
    private final static String savePath =
            "/Users/jiangzilai/Documents/book";

    public static void main(String[] args) throws IOException, InterruptedException {

        FastDownloader downloader = new Biquge(bookName, savePath);

        //下载全部内容到一个txt文件里
        downloader.downloadTXT();
    }
}
