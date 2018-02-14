import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

    private final static int TYPE_INCLUDE = 1;
    private final static int TYPE_EXCEPT = 2;
    public static boolean openLog = false;

    //包含正则表达式
    private static String Include(String start, String end) {
        return start + "[\\s\\S]*?" + end;
    }

    //不包含正则表达式
    private static String Except(String start, String end) {
        return "(?<=" + start + ")[\\s\\S]*?(?=" + end + ")";
    }

    //解析逻辑
    private static List<String> regex(String start, String end, String source, int type) {
        List<String> list = new ArrayList<>();
        String regEx;
        //设置解析方式
        if (type == TYPE_EXCEPT) {
            regEx = Except(start, end);
        } else regEx = Include(start, end);
        Pattern pattern = Pattern.compile(regEx);
        //将目标集合里的所有内容解析出来，放到这里的list里
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            if (matcher.group() != null) {
                list.add(matcher.group());
            }
        }
        return list;
    }

    public static List<String> regexInclude(String start, String end, String source) {
        return regex(start, end, source, TYPE_INCLUDE);
    }

    public static List<String> regexExcept(String start, String end, String source) {
        return regex(start, end, source, TYPE_EXCEPT);
    }

    public static String getHtml(String url) {
        return getHtml(url, "utf-8");
    }

    public static String getHtml(String url, String encodeType) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Charsert", "UTF-8");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), encodeType));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        if (openLog) System.out.println(result);
        return result.toString();
    }
}
