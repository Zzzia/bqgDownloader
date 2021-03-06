## 小说一键下载工具

已支持网站：

* [笔趣阁](http://www.biquge.com.tw)
* [笔神阁](http://www.bishenge.com)
* [看神作(推荐)](http://www.kanshenzuo.com)
* [南山书院](https://www.szyangxiao.com)
* [E8中文网](http://www.e8zw.com)

#### 目前功能：一键下载并生成格式规范的txt和epub格式的小说

##### 并发下载，错误重试，理论能达到满速，实测下载速度2m/s以上

###### 网络请求采用okHttp，需要添加kotlin支持，最新版idea自带该插件，在KotlinPlugin文件里指定即可

###### 目前未适配转换mobi。有需要请自行下载[kindlegen](https://www.amazon.com/gp/feature.html?ie=UTF8&docId=1000765211)软件，用epub转mobi，效果很好

--- 

实现原理：

解析小说网站的章节目录，保存所有章节名字以及url，然后再依次解析每一章的文字，按行保存，最后转换为不同格式的文件

###### 使用示例：更改保存路径以及小说名称即可

```java
public class Main {

    //这两个参数请自行更换
    private final static String bookName = "修真聊天群";
    //存放目录，该目录是mac系统下的，windows需要自行更正“\\”
    private final static String savePath = "/Users/zia/Documents/book";

    public static void main(String[] args) throws IOException, InterruptedException {

        FastDownloader downloader = new Kanshenzuo(bookName, savePath);

        //下载全部内容到一个txt文件里
        //downloader.downloadTXT();
        //下载epub格式，自动生成索引
        downloader.downloadEPUB();
        //下载epub并转换为mobi格式
        //downloader.downloadMOBI();
    }
}

```

若书名搜索结果重复，会导致解析失败。该情况下需要手动配置网页目录

首先用浏览器打开小说网站（笔神阁）[http://www.bishenge.com/](http://www.bishenge.com/)

假设搜索 校花的贴身高手 搜索结果将会是多个，选择你想要的，打开进入目录页面，如[http://www.bishenge.com/1_1693/](http://www.bishenge.com/1_1693/)

将这个网址复制下来放在如下的参数里就行了

###### 使用如下代码即可

```java
public class Main {

    private final static String bookName = "校花的贴身高手";
    private final static String savePath = "/Users/jiangzilai/Documents/book";

    public static void main(String[] args) throws IOException, InterruptedException {

        FastDownloader downloader = new Bishenge(bookName, "http://www.bishenge.com/1_1693/", savePath);

        //下载epub格式，自动生成索引
        downloader.downloadEPUB();
    }
}
```

