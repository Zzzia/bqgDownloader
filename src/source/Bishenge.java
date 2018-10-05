package source;

import bean.ChapterBuffer;
import bean.Chapter;
import util.RegexUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created By zia on 2018/10/5.
 * 笔神阁  http://www.bishenge.com
 * 测试约1.5m/s
 */
public class Bishenge extends FastDownloader {

    public Bishenge(String bookName, String catalogUrl, String path) {
        super(bookName, catalogUrl, path);
    }

    @Override
    protected List<Chapter> getChapters(String catalogUrl) throws IOException {
        String catalogHTML = getHtml(catalogUrl);
        String sub = RegexUtil.regexExcept("<div id=\"list\">", "</div>", catalogHTML).get(0);
        String ssub = sub.split("正文</dt>")[1];
        List<String> as = RegexUtil.regexInclude("<a", "</a>", ssub);
        List<Chapter> list = new ArrayList<>();
        as.forEach(s -> {
            RegexUtil.Tag tag = new RegexUtil.Tag(s);
            Chapter chapter = new Chapter();
            chapter.name = tag.getText();
            chapter.href = catalogUrl + tag.getValue("href");
            list.add(chapter);
        });
        return list;
    }

    @Override
    protected ChapterBuffer adaptBookBuffer(Chapter chapter, int num) throws IOException {
        ChapterBuffer chapterBuffer = new ChapterBuffer();
        chapterBuffer.number = num;
        chapterBuffer.name = chapter.name;

        StringBuilder sb = new StringBuilder();
        String html = getHtml(chapter.href);

        String sub = RegexUtil.regexExcept("<div id=\"content\">","</div>",html).get(0);

        String lines[] = sub.split("<br>|<br/>|<br />");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) continue;
            sb.append(line.replaceAll("&nbsp;",""));
            sb.append("\n");
        }
        chapterBuffer.content = sb.toString();
        return chapterBuffer;
    }
}