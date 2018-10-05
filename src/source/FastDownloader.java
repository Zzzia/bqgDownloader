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

    public void download() throws IOException, InterruptedException {
        String filePath = path + "/" + bookName + ".txt";
        //有序章节
        List<Chapter> chapters = getChapters(catalogUrl);
        LinkedBlockingDeque<ChapterBuffer> chapterBuffers = new LinkedBlockingDeque<>(capacity);

        ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(chapters.size());

        for (int i = 0; i < chapters.size(); i++) {
            int finalI = i;
            threadPool.execute(() -> {
                try {
                    chapterBuffers.add(adaptBookBuffer(chapters.get(finalI), finalI));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("出错章节：" + chapters.get(finalI));
                }
                countDownLatch.countDown();
                System.out.println(chapters.get(finalI).name);
            });
        }
        countDownLatch.await();
        List<ChapterBuffer> books = new ArrayList<>(capacity);
        books.addAll(chapterBuffers);
        books.sort(Comparator.comparingInt(o -> o.number));
        BufferedWriter bufferedWriter =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
        books.forEach(chapter -> {
            try {
                bufferedWriter.write(chapter.name + "\n");
                bufferedWriter.write(chapter.content + "\n");
            } catch (IOException e) {
                System.out.println(chapter);
                e.printStackTrace();
            }
        });
        threadPool.shutdown();
        bufferedWriter.close();
        System.out.println("全部下载完成");
    }

    protected String getHtml(String html) throws IOException {
        return RegexUtil.getHtml(html, "GBK");
    }

    protected void setThreadCount(int count){
        threadCount = count;
    }
}
