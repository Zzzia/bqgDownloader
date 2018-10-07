package source;

import bean.Chapter;
import bean.ChapterBuffer;
import util.RegexUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created By zia on 2018/10/5.
 * 看神作 http://www.kanshenzuo.com
 * 书挺全的
 * 测试约500k/s
 */
public class Kanshenzuo extends FastDownloader {

    public Kanshenzuo(String bookName, String catalogUrl, String path) {
        super(bookName, catalogUrl, path);
    }

    @Override
    protected List<Chapter> getChapters(String catalogUrl) throws IOException {
        String catalogHTML = getHtml(catalogUrl);
        String sub = RegexUtil.regexExcept("<div id=\"list\">", "</div>", catalogHTML).get(0);
        String ssub = sub.split("正文</dt>")[1];
        List<String> as = RegexUtil.regexInclude("<a", "</a>", ssub);
        List<Chapter> list = new ArrayList<>();
        final String root = "http://www.kanshenzuo.com";
        as.forEach(s -> {
            RegexUtil.Tag tag = new RegexUtil.Tag(s);
            Chapter chapter = new Chapter();
            chapter.name = tag.getText();
            chapter.href = root + tag.getValue("href");
            list.add(chapter);
        });
        return list;
    }

    @Override
    protected ChapterBuffer adaptBookBuffer(Chapter chapter, int num) throws IOException {
        ChapterBuffer chapterBuffer = new ChapterBuffer();
        chapterBuffer.number = num;
        chapterBuffer.name = chapter.name;

        String html = getHtml(chapter.href);

        String sub = RegexUtil.regexExcept("<div id=\"content\">","</div>",html).get(0);

        String lines[] = sub.split("<br>|<br/>|<br />");

        List<String> content = new ArrayList<>();

        for (String line : lines) {
            if (!line.isEmpty()){
                content.add(cleanContent(line));
            }
        }

        chapterBuffer.content = content;
        return chapterBuffer;
    }
}
