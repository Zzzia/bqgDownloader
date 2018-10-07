package source;

import bean.Chapter;
import bean.ChapterBuffer;
import util.RegexUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created By zia on 2018/10/5.
 * 网站解析抽象父类
 */
public abstract class FastDownloader {

    private String bookName;
    private String catalogUrl;
    private String path;
    private int capacity = 3000;
    private int threadCount = 100;

    public FastDownloader(String bookName, String catalogUrl, String path) {
        this.bookName = bookName;
        this.catalogUrl = catalogUrl;
        this.path = path;
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                System.out.println("文件路径创建失败");
            }
        }
    }

    protected abstract List<Chapter> getChapters(String catalogUrl) throws IOException;

    protected abstract ChapterBuffer adaptBookBuffer(Chapter chapter, int num) throws IOException;

    public void downloadTXT() throws IOException, InterruptedException {
        String filePath = path + "/" + bookName + ".txt";

        //从目录页获取有序章节
        List<Chapter> chapters = getChapters(catalogUrl);

        //并发下载所有章节，根据顺序排序
        List<ChapterBuffer> books = downloadChapter(chapters);

        BufferedWriter bufferedWriter =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));

        books.forEach(chapter -> {
            try {
                //格式化
                //章节名+换行+空行
                bufferedWriter.write(chapter.name);
                bufferedWriter.write("\n\n");
                for (String line : chapter.content) {
                    //4个空格+正文+换行+空行
                    bufferedWriter.write("    ");
                    bufferedWriter.write(line);
                    bufferedWriter.write("\n\n");
                }
                //章节结束空三行，用来分割下一章节
                bufferedWriter.write("\n\n\n");
            } catch (IOException e) {
                System.out.println("写入出错 ： " + chapter);
                e.printStackTrace();
            }
        });
        bufferedWriter.close();
        System.out.println("保存完成 ： " + filePath);
    }

    public void downloadEPUB() {

    }

    /**
     * 获取章节内容
     */
    private List<ChapterBuffer> downloadChapter(List<Chapter> chapters) throws InterruptedException {
        //非阻塞集合，临时装一下ChapterBuffer
        LinkedBlockingDeque<ChapterBuffer> chapterBuffers = new LinkedBlockingDeque<>(capacity);

        //线程 并发结束标志
        ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(chapters.size());

        //记录错误数量
        AtomicInteger error = new AtomicInteger();
        error.set(0);

        for (int i = 0; i < chapters.size(); i++) {
            int finalI = i;
            threadPool.execute(() -> {
                try {
                    //章节html解析，需要实现抽象类
                    chapterBuffers.add(adaptBookBuffer(chapters.get(finalI), finalI));
                } catch (IOException e) {
                    e.printStackTrace();
                    error.addAndGet(1);
                    System.out.println("出错章节 ： " + chapters.get(finalI));
                }
                countDownLatch.countDown();
                System.out.println(chapters.get(finalI).name);
            });
        }
        //等待全部下载完毕
        countDownLatch.await();
        threadPool.shutdown();
        System.out.println("下载完成，出错数量 ： " + error.get());
        //装在List里，并根据number排序返回
        List<ChapterBuffer> books = new ArrayList<>(capacity);
        books.addAll(chapterBuffers);
        books.sort(Comparator.comparingInt(o -> o.number));
        return books;
    }

    protected String getHtml(String html) throws IOException {
        return RegexUtil.getHtml(html, "GBK");
    }

    protected String cleanContent(String content) {
        return content.replaceAll("&nbsp;|<br>|<br/>|<br />", "");
    }

    public void setThreadCount(int count) {
        threadCount = count;
    }
}
