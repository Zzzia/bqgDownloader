package bean;

/**
 * 从目录页解析出来的章节名以及章节链接
 */
public class Chapter {
    public String name;
    public String href;
    public int num = -1;

    @Override
    public String toString() {
        return name + "  " + href;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
