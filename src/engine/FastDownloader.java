package engine;

import bean.ChapterBuffer;
import tool.FoxEpubWriter;

import java.io.*;
import java.util.List;

/**
 * Created By zia on 2018/10/5.
 * 快速下载指定格式小说的框架
 */
public abstract class FastDownloader implements CustomRegex {

    private String bookName;
    private String catalogUrl;
    private String path;
    //OkHttp3最大并发访问数量其实是5，请不要修改。经过血泪教训，超过可能被拉入黑名单
    //增加该线程的数量是为了加快正则解析速度，从而提高访问速度，玄学调参，请勿修改
    private int threadCount = 300;

    public FastDownloader(String bookName, String catalogUrl, String path) {
        this.bookName = bookName;
        this.catalogUrl = catalogUrl;
        this.path = path;
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                System.out.println("文件路径创建失败");
            }
        }
    }

    public void downloadTXT() throws IOException, InterruptedException {
        String filePath = path + File.separator + bookName + ".txt";

        Downloader downloader = new Downloader(catalogUrl, this, threadCount);
        List<ChapterBuffer> books = downloader.download();

        BufferedWriter bufferedWriter =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));

        books.forEach(chapter -> {
            try {
                //格式化
                //章节名+换行+空行
                bufferedWriter.write(chapter.name);
                bufferedWriter.write("\n\n");
                for (String line : chapter.content) {
                    //4个空格+正文+换行+空行
                    bufferedWriter.write("    ");
                    bufferedWriter.write(line);
                    bufferedWriter.write("\n\n");
                }
                //章节结束空三行，用来分割下一章节
                bufferedWriter.write("\n\n\n");
            } catch (IOException e) {
                System.out.println("写入出错 ： " + chapter);
                e.printStackTrace();
            }
        });
        bufferedWriter.close();
        System.out.println("保存完成 ： " + filePath);
    }

    public void downloadEPUB() throws IOException, InterruptedException {
        saveKindle(false);
    }

    public void downloadMOBI() throws IOException, InterruptedException {
//        saveKindle(true);
        System.out.println("自行下载kindlegen软件吧...不同平台不好适配。epub转mobi，效果很好...");
        System.exit(0);
    }

    private void saveKindle(boolean isMOBI) throws IOException, InterruptedException {
        String name;
        if (isMOBI) {
            name = bookName + ".mobi";
        } else {
            name = bookName + ".epub";
        }
        String filePath = path + File.separator + name;

        FoxEpubWriter foxEpubWriter = new FoxEpubWriter(new File(filePath), name);

        if (isMOBI) {
            foxEpubWriter.setEpub(false);
        } else {
            foxEpubWriter.setEpub(true);
        }

        Downloader downloader = new Downloader(catalogUrl, this, threadCount);
        List<ChapterBuffer> books = downloader.download();

        books.forEach(chapterBuffer -> {
            StringBuilder content = new StringBuilder();
            for (String line : chapterBuffer.content) {
                content.append("<p>");
                content.append("    ");
                content.append(line);
                content.append("</p>");
            }
            foxEpubWriter.addChapter(chapterBuffer.name, content.toString());
        });

        foxEpubWriter.saveAll();
        System.out.println("保存成功 : " + filePath);
    }

    //使用okHttp3的网络请求封装，默认使用
    protected String getHtml(String html) throws IOException {
        return NetUtil.getHtml(html);
    }

    protected String getHtml(String html, String encodeType) throws IOException {
        return NetUtil.getHtml(html, encodeType);
    }

    /**
     * 预留的一个清除乱码或者html格式的方法
     */
    protected String cleanContent(String content) {
        return content.replaceAll("\n|\t|\r|&nbsp;|<br>|<br/>|<br />|p&gt;|&gt;", "").trim();
    }

    /**
     * 设置下载的并发数量，有的网站服务器质量差，需要设置小点
     */
    public void setThreadCount(int count) {
        threadCount = count;
    }
}
