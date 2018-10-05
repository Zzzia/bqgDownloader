package bean;

/**
 * Created By zia on 2018/10/5.
 */
public class ChapterBuffer {
    public int number;//章节在小说中的顺序，最后排序需要用到
    public String name;//章节名字
    public String content;//章节内容

    @Override
    public String toString() {
        return "bean.ChapterBuffer{" +
                "number=" + number +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
