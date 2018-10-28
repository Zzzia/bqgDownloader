package engine;

import bean.Chapter;
import bean.ChapterBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created By zia on 2018/10/28.
 * 带重试队列的并发下载工具
 */
class Downloader {

    private ArrayList<ChapterBuffer> chapterBuffers;
    private LinkedList<Chapter> queue;
    private final Object bufferLock = new Object();
    private final Object queueLock = new Object();

    private String url;
    private CustomRegex regex;
    private ExecutorService threadPool;

    Downloader(String url, CustomRegex customRegex, int threadCount) {
        this.url = url;
        this.regex = customRegex;
        this.threadPool = Executors.newFixedThreadPool(threadCount);
    }

    public ArrayList<ChapterBuffer> download() throws IOException, InterruptedException {
        System.out.println("解析目录...");

        //从目录页获取有序章节
        List<Chapter> chapters = regex.getChapters(url);

        int size = chapters.size();

        if (size == 0) {
            System.err.println("解析目录失败");
            System.exit(0);
        }

        chapterBuffers = new ArrayList<>(size + 1);
        queue = new LinkedList<>(chapters);

        System.out.println("一共" + size + "张，开始下载...");

        int index = 0;

        while (getBufferSize() < size) {
            synchronized (queueLock) {//一旦拿到锁，下载队列中所有的章节
                Chapter chapter = queue.poll();
                if (chapter == null) {//如果队列为空，释放锁
                    Thread.sleep(1000);
                } else {//队列有章节，下载所有
                    while (chapter != null) {
                        Chapter finalChapter = chapter;
                        if (finalChapter.num == -1) {
                            finalChapter.num = index++;
                        }
                        threadPool.execute(() -> {
                            try {
                                ChapterBuffer buffer = regex.adaptBookBuffer(finalChapter, finalChapter.num);
                                System.out.println(finalChapter.name);
                                addBuffer(buffer);
                            } catch (IOException e) {
                                System.err.println(e.getMessage());
                                System.err.println("重试章节 ： " + finalChapter);
                                addQueue(finalChapter);//重新加入队列，等待下载
                            }
                        });
                        chapter = queue.poll();
                    }
                }
            }
        }
        threadPool.shutdown();
        chapterBuffers.sort(Comparator.comparingInt(o -> o.number));
        System.out.println("下载完成(" + chapterBuffers.size() + ")，正在保存");
        return chapterBuffers;
    }

    private void addBuffer(ChapterBuffer chapterBuffer) {
        synchronized (bufferLock) {
            chapterBuffers.add(chapterBuffer);
        }
    }

    private int getBufferSize() {
        synchronized (bufferLock) {
            return chapterBuffers.size();
        }
    }

    private void addQueue(Chapter chapter) {
        synchronized (queueLock) {
            queue.offer(chapter);
        }
    }
}
