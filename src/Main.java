import java.io.*;

public class Main {

    //这两个参数请自行更换
    private final static String bookName = "斗罗大陆III龙王传说";
    //存放目录，实例目录是mac系统下的，windows需要自行更正“\\”
    private final static String savePath =
            "/Users/jiangzilai/Documents/book/斗罗大陆III龙王传说";

    public static void main(String[] args) throws IOException {
        BqgDownloader bqgDownloader = new BqgDownloader(bookName, savePath);
        bqgDownloader.downloadAll2txt();//下载全部内容到一个txt文件里

        /*
        还有两个方法供选择
        bqgDownloader.downloadAll();//一章一章地下载全部内容
        bqgDownloader.downloadPart(0,50);//下载1-51章，50个txt，没做越界处理

        若笔趣阁搜索结果只有一个，那么可以直接如上方法输入汉字下载
        若搜索结果不止一个的时候，需要手动获取网址。。
        在浏览器上手动搜索一下，把链接复制下来，
        如{http://www.biquge.com.tw/16_16288/}
        这样的目录网址，也可以解析
        然后添加下面的方法
        bqgDownloader.setExactUrl("http://www.biquge.com.tw/16_16288/");
        bqgDownloader.downloadAll2txt();
        */
    }
}
