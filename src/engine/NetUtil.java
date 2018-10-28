package engine;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created By zia on 2018/10/21.
 */
public class NetUtil {
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            //请设置代理，否则会被小说网站ban的...量小没关系
//            .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("106.75.226.36", 808)))
            .build();

    /**
     * 同步获取html文件，默认编码gbk
     */
    public static String getHtml(String url) throws IOException {
        return getHtml(url, "gbk");
    }

    public static String getHtml(String url, String encodeType) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("accept", "*/*")
                .addHeader("connection", "Keep-Alive")
                .addHeader("Charsert", "UTF-8")
                .addHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
                .build();
        ResponseBody body = okHttpClient.newCall(request).execute().body();
        if (body == null) {
            return "";
        } else {
            return new String(body.bytes(), encodeType);
        }
    }

    private static Random mRandom = new Random();

    /**
     * 获取随机ip地址
     *
     * @return random ip
     */
    private static String getRandomIPAddress() {
        return String.valueOf(mRandom.nextInt(255)) + "." + String.valueOf(mRandom.nextInt(255)) + "." + String.valueOf(mRandom.nextInt(255)) + "." + String.valueOf(mRandom.nextInt(255));
    }
}
