package engine;

import bean.Chapter;
import bean.ChapterBuffer;
import tool.FoxEpubWriter;

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
    private int capacity = 10000;
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

    /**
     * 根据小说目录页面解析所有的小说章节
     *
     * @param catalogUrl 目录地址
     * @return Chapter集合
     */
    protected abstract List<Chapter> getChapters(String catalogUrl) throws IOException;

    /**
     * 通过小说章节解析这一章节的html文字，保存在ChapterBuffer中
     *
     * @param chapter 小说章节 包括了url和标题
     * @param num     序号，之后排序的标准
     * @return ChapterBuffer集合
     */
    protected abstract ChapterBuffer adaptBookBuffer(Chapter chapter, int num) throws IOException;

    public void downloadTXT() throws IOException, InterruptedException {
        String filePath = path + File.separator + bookName + ".txt";

        System.out.println("解析目录...");

        //从目录页获取有序章节
        List<Chapter> chapters = getChapters(catalogUrl);

        System.out.println("开始下载章节...");

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

    public void downloadEPUB() throws IOException, InterruptedException {
        saveKindle(false);
    }

    public void downloadMOBI() throws IOException, InterruptedException {
//        saveKindle(true);
        System.out.println("自行下载kindlegen软件吧...不同平台不好适配。epub转mobi，效果很好...");
        System.exit(0);
    }

    private void saveKindle(boolean isMOBI) throws IOException, InterruptedException {
        String name;
        if (isMOBI) {
            name = bookName + ".mobi";
        } else {
            name = bookName + ".epub";
        }
        String filePath = path + File.separator + name;

        System.out.println("解析目录...");

        //从目录页获取有序章节
        List<Chapter> chapters = getChapters(catalogUrl);

        System.out.println("开始下载章节...");

        //并发下载所有章节，根据顺序排序
        List<ChapterBuffer> books = downloadChapter(chapters);

        FoxEpubWriter foxEpubWriter = new FoxEpubWriter(new File(filePath), name);

        if (isMOBI) {
            foxEpubWriter.setEpub(false);
        } else {
            foxEpubWriter.setEpub(true);
        }

        books.forEach(chapterBuffer -> {
            StringBuilder content = new StringBuilder();
            for (String line : chapterBuffer.content) {
                content.append("<p>");
                content.append("    ");
                content.append(line);
                content.append("</p>");
            }
            foxEpubWriter.addChapter(chapterBuffer.name, content.toString());
        });

        foxEpubWriter.saveAll();
        System.out.println("保存成功 : " + filePath);
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

        LinkedBlockingDeque<Chapter> errorChapters = new LinkedBlockingDeque<>(capacity);

        for (int i = 0; i < chapters.size(); i++) {
            int finalI = i;
            threadPool.execute(() -> {
                try {
                    //章节html解析，需要实现抽象类
                    Chapter chapter = chapters.get(finalI);
                    chapter.num = finalI;
                    chapterBuffers.add(adaptBookBuffer(chapter, finalI));
                    System.out.println(chapter.name);
                } catch (IOException e) {
                    error.getAndIncrement();
                    errorChapters.add(chapters.get(finalI));
                    System.out.println("重试章节 ： " + chapters.get(finalI));
                }
                countDownLatch.countDown();
            });
        }
        //等待全部下载完毕
        countDownLatch.await();
        //重试错误章节
        System.out.println("开始重试");
        CountDownLatch errorDownLatch = new CountDownLatch(errorChapters.size());
        for (Chapter errorChapter : errorChapters) {
            threadPool.execute(() -> {
                try {
                    //章节html解析，需要实现抽象类
                    chapterBuffers.add(adaptBookBuffer(errorChapter, errorChapter.num));
                    System.out.println(errorChapter.name);
                    error.getAndDecrement();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("出错章节 ： " + errorChapter);
                }catch (IndexOutOfBoundsException e1){//数组越界，说明解析错误
                    e1.printStackTrace();
                    System.out.println("解析错误，请修改代码");
                    System.exit(0);
                }
                errorDownLatch.countDown();
            });
        }
        errorDownLatch.await();
        //关闭线程池
        threadPool.shutdown();
        System.out.println("下载完成，出错数量 ： " + error.get());
        System.out.println("正在保存...");
        //装在List里，并根据number排序返回
        List<ChapterBuffer> books = new ArrayList<>(capacity);
        books.addAll(chapterBuffers);
        books.sort(Comparator.comparingInt(o -> o.number));
        return books;
    }

    //使用okHttp3的网络请求封装，默认使用
    protected String getHtml(String html) throws IOException {
        return NetUtil.getHtml(html);
    }

    protected String getHtml(String html, String encodeType) throws IOException {
        return NetUtil.getHtml(html, encodeType);
    }

    /**
     * 预留的一个清除乱码或者html格式的方法
     */
    protected String cleanContent(String content) {
        return content.replaceAll("\n|\t|\r|&nbsp;|<br>|<br/>|<br />|p&gt;|&gt;", "").trim();
    }

    /**
     * 设置下载的并发数量，有的网站服务器质量差，需要设置小点
     */
    public void setThreadCount(int count) {
        threadCount = count;
    }
}
