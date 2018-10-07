package bean;

import java.util.List;

/**
 * Created By zia on 2018/10/5.
 * 每一章节的内容
 */
public class ChapterBuffer {
    public int number;//章节在小说中的顺序，最后排序需要用到
    public String name;//章节名字
    public List<String> content;//章节内容 按行分

    @Override
    public String toString() {
        return "bean.ChapterBuffer{" +
                "number=" + number +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
