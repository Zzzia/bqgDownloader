import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 笔趣阁小说下载
 */
public class BqgDownloader {
    private final String root = "http://www.biquge.com.tw";
    private String bookName;
    private String path;
    private String exactUrl;

    public BqgDownloader(String bookName, String path) {
        this.bookName = bookName;
        this.path = path;
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                System.out.println("文件路径创建失败");
            }
        }
    }

    public void setExactUrl(String exactUrl){
        this.exactUrl = exactUrl;
    }

    private String getCatalogUrl() throws UnsupportedEncodingException {
        if (exactUrl != null) return exactUrl;
        return root + "/modules/article/soshu.php?searchkey=+"
                + URLEncoder.encode(bookName, "GBK");
    }

    private List<Chapter> getChapters(String url) {
        String catalogHTML = RegexUtil.getHtml(url, "GBK");
        List<String> as = RegexUtil.regexExcept("<dd>", "</dd>", catalogHTML);
        List<Chapter> chapters = new ArrayList<>();
        for (String a : as) {
            Chapter chapter = new Chapter();
            chapter.name = RegexUtil.regexExcept("\">", "</a>", a).get(0);
            chapter.href = root + RegexUtil.regexExcept("<a href=\"", "\">", a).get(0);
            chapters.add(chapter);
        }
        System.out.println(chapters);
        return chapters;
    }

    private void downloadSingle(Chapter chapter) throws IOException {
        String filePath = path + "/" + chapter.name + ".txt";
        String html = RegexUtil.getHtml(chapter.getHref(), "GBK");
        List<String> contents = RegexUtil.regexExcept("&nbsp;&nbsp;&nbsp;&nbsp;", "<br />", html);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath)));
        for (String content : contents) {
            bufferedWriter.write(content + "\n");
        }
        bufferedWriter.close();
        System.out.println(chapter.name);
    }

    public void downloadPart(int start, int end) throws IOException {
        List<Chapter> chapters = getChapters(getCatalogUrl());
        for (int i = start; i < end; i++) {
            downloadSingle(chapters.get(i));
        }
        OK();
    }

    public void downloadAll() throws IOException {
        List<Chapter> chapters = getChapters(getCatalogUrl());
        for (Chapter chapter : chapters) {
            downloadSingle(chapter);
        }
        OK();
    }

    public void downloadAll2txt() throws IOException {
        String filePath = path + "/" + bookName + ".txt";
        List<Chapter> chapters = getChapters(getCatalogUrl());
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath)));
        for (Chapter chapter : chapters) {
            String html = RegexUtil.getHtml(chapter.getHref(), "GBK");
            List<String> contents = RegexUtil.regexExcept("&nbsp;&nbsp;&nbsp;&nbsp;", "<br />", html);
            bufferedWriter.write("\n\n" + chapter.name + "\n");
            for (String content : contents) {
                bufferedWriter.write(content + "\n");
            }
            System.out.println(chapter.name + ".txt");
        }
        bufferedWriter.close();
        OK();
    }

    private void OK(){
        System.out.println("全部下载完成");
    }
}
