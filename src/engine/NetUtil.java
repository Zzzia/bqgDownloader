package engine;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created By zia on 2018/10/21.
 */
public class NetUtil {
    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 同步获取html文件，默认编码utf-8
     */
    public static String getHtml(String url) throws IOException {
        return getHtml(url, "gb2312");
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
}
