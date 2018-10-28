package engine;

import bean.Chapter;
import bean.ChapterBuffer;

import java.io.IOException;
import java.util.List;

public interface CustomRegex {
    /**
     * 根据小说目录页面解析所有的小说章节
     *
     * @param catalogUrl 目录地址
     * @return Chapter集合
     */
    List<Chapter> getChapters(String catalogUrl) throws IOException;

    /**
     * 通过小说章节解析这一章节的html文字，保存在ChapterBuffer中
     *
     * @param chapter 小说章节 包括了url和标题
     * @param num     序号，之后排序的标准
     * @return ChapterBuffer集合
     */
    ChapterBuffer adaptBookBuffer(Chapter chapter, int num) throws IOException;
}
