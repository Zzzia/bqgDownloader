package source;

import bean.Chapter;
import bean.ChapterBuffer;
import engine.FastDownloader;
import util.RegexUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created By byakuya on 2018/10/5.
 * E8中文  http://www.e8zw.com
 * 测试约满速
 */

public class E8 extends FastDownloader {

    public E8(String bookName, String catalogUrl, String path) {
        super(bookName, catalogUrl, path);
    }

    @Override
    protected List<Chapter> getChapters(String catalogUrl) throws IOException {
        String html = getHtml(catalogUrl, "utf-8");
        String first = RegexUtil.regexExcept("<div id=\"list\">", "<script>", html).get(0);
        String dirtys = RegexUtil.regexInclude("</dt>", "<dt>", first).get(0);//删除最新章节
        String ddHtml = first.substring(dirtys.length());
        List<String> DDs = RegexUtil.regexExcept("<dd>", "</dd>", ddHtml);
        List<Chapter> chapters = new ArrayList<>();
        for (String dd : DDs) {
            String href = catalogUrl + RegexUtil.regexExcept("href=\"", "\">", dd).get(0);
            String name = RegexUtil.regexExcept(">", "<", dd).get(0);
            Chapter chapter = new Chapter();
            chapter.setHref(href);
            chapter.setName(name);
            chapters.add(chapter);
        }
        return chapters;
    }

    @Override
    protected ChapterBuffer adaptBookBuffer(Chapter chapter, int num) throws IOException {
        String html = getHtml(chapter.href, "utf-8");
        String contents = RegexUtil.regexExcept("<div id=\"content\">", "</div>", html).get(0);
        String texts[] = contents.split("<br>|<br/>");

        ChapterBuffer buffer = new ChapterBuffer();
        List<String> lines = new ArrayList<>();
        for (String text : texts) {
            if (!text.trim().isEmpty()) {
                lines.add(cleanContent(text));
            }
        }

        buffer.content = lines;
        buffer.name = chapter.name;
        buffer.number = num;
        return buffer;
    }
}
