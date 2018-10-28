package source;

import bean.Chapter;
import bean.ChapterBuffer;
import engine.FastDownloader;
import util.RegexUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created By zia on 2018/10/5.
 * 南山书院 https://www.szyangxiao.com/
 * 需要控制并发数量为1，并且隔几秒才能刷新下一张，因此下载很慢
 * 不建议使用，太慢了
 */
public class Nanshanshuyuan extends FastDownloader {

    private static final String root = "https://www.szyangxiao.com";

    public Nanshanshuyuan(String bookName, String catalogUrl, String path) {
        super(bookName, catalogUrl, path);
        setThreadCount(1);
    }

    @Override
    public List<Chapter> getChapters(String catalogUrl) throws IOException {
        String html = getHtml(catalogUrl);
        String sub = RegexUtil.regexExcept("<ul class=\"nav clearfix\">", "</ul>", html).get(0);

        List<String> lines = RegexUtil.regexInclude("<a", "</a>", sub);
        List<Chapter> chapters = new ArrayList<>(1000);

        lines.forEach(line -> {
            RegexUtil.Tag tag = new RegexUtil.Tag(line);
            Chapter chapter = new Chapter();
            chapter.href = root + tag.getValue("href");
            chapter.name = tag.getText();
            chapters.add(chapter);
        });
        return chapters;
    }

    @Override
    public ChapterBuffer adaptBookBuffer(Chapter chapter, int num) throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String html = getHtml(chapter.href);
        String sub = RegexUtil.regexExcept("<div id=\"content\"", "</div>", html).get(0);
        List<String> ps = RegexUtil.regexExcept("<p>", "</p>", sub);

        List<String> content = new ArrayList<>();

        ps.forEach(line -> {
            if (!line.trim().isEmpty()){
                content.add(cleanContent(line));
            }
        });

        ChapterBuffer chapterBuffer = new ChapterBuffer();
        chapterBuffer.content = content;
        chapterBuffer.name = chapter.name;
        chapterBuffer.number = num;
        return chapterBuffer;
    }
}
