package source;

import bean.Chapter;
import bean.ChapterBuffer;
import util.RegexUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created By byakuya on 2018/10/5.
 * E8中文  http://www.e8zw.com
 * 测试约满速
 */

public class E8 extends FastDownloader {

    public E8(String bookName, String catalogUrl, String path) {
        super(bookName, catalogUrl, path);
        setThreadCount(20);
    }

    @Override
    protected List<Chapter> getChapters(String catalogUrl) throws IOException {
        String html = RegexUtil.getHtml(catalogUrl);
        String first = RegexUtil.regexExcept("<div id=\"list\">", "</div>", html).get(0);
        System.out.println(first);
        String ddHtml = RegexUtil.regexExcept("<dt>《逆天邪神》</dt>", "</dl>", first).get(0);
        List<String> DDs = RegexUtil.regexExcept("<dd>", "</dd>", ddHtml);
        List<Chapter> chapters = new ArrayList<>();
        for (String dd : DDs) {
            String href = "https://www.e8zw.com/book/0/560/" + RegexUtil.regexExcept("href=\"", "\">", dd).get(0);
            String name = RegexUtil.regexExcept(">", "<", dd).get(0);
            Chapter chapter = new Chapter();
            chapter.setHref(href);
            chapter.setName(name);
            chapters.add(chapter);
            System.out.println(chapter.toString());
        }
        return chapters;
    }

    @Override
    protected ChapterBuffer adaptBookBuffer(Chapter chapter, int num) throws IOException {
        String html = RegexUtil.getHtml(chapter.href);
        String first = RegexUtil.regexExcept("<div id=\"content\">", "</div>", html).get(0);
        String texts[] = first.split("<br>|<br/>");
        ChapterBuffer buffer = new ChapterBuffer();
        List<String> lines = new ArrayList<>();

        for (String text : texts) {
            if (text.length() != 0) {
                lines.add(cleanContent(text));
            }
        }

        buffer.content = lines;
        buffer.name = chapter.name;
        buffer.number = num;
        return buffer;
    }
}
