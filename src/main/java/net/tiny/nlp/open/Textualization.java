package net.tiny.nlp.open;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Textualization {

    private static final Logger LOGGER = Logger.getLogger(Textualization.class.getName());

    public static final String BROWSER_USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:24.0) Gecko/20190101 Firefox/24.0";
    public static final String DEFAULT_USER_AGENT = "WikiClient/1.0";
    private static final int BUFFER_SIZE = 8192*2; //16K
    // 定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
    private static final String REGEX_SCRIPT = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
    // 定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
    private static final String REGEX_STYLE = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
    // 定义HTML标签的正则表达式
    private static final String REGEX_HTML = "<[^>]+>"; //"</?[^>]+>"
    // 定义连续空格行的正则表达式
    private static final String REGEX_BLANKS = "[ ]+";
    // 定义空格,回车,换行符,制表符的正则表达式
    private static final String REGEX_LFS = "(?m)^\\s*$(\\n|\\r\\n)";

    /**
     * 从网页中提取纯文本
     * @param content
     * @return 纯文本
     */
    public static String text(String content) {
        String text = content; // 含<HTML>标签的字符串
        // 过滤script标签
        text = Pattern.compile(REGEX_SCRIPT, Pattern.CASE_INSENSITIVE).matcher(text).replaceAll("");
        // 过滤style标签
        text = Pattern.compile(REGEX_STYLE, Pattern.CASE_INSENSITIVE).matcher(text).replaceAll("");
        // 过滤HTML标签
        text = Pattern.compile(REGEX_HTML, Pattern.CASE_INSENSITIVE).matcher(text).replaceAll("");
        // 过滤连续空格行
        text = text.replaceAll(REGEX_BLANKS, " ");
        // 过滤连续换行，返回文本字符串
        return text.replaceAll(REGEX_LFS, "");
    }

    public static String text(URL url) {
        return text(url, "UTF-8");
    }

    public static String text(URL url, String enc) {
        try {
            return text(new String(fetch(url, DEFAULT_USER_AGENT), enc));
        } catch (IOException e) {
            //Warning
            LOGGER.warning(String.format("%s : '%s'", url.toString(), e.getMessage()));
            return null;
        }
    }

    public static String fetch(String url) {
        try {
            return new String(fetch(new URL(new URI(url).toASCIIString()), DEFAULT_USER_AGENT));
        } catch (IOException | URISyntaxException e) {
            //Warning
            LOGGER.warning(String.format("%s : '%s'", url.toString(), e.getMessage()));
            return null;
        }
    }

    static byte[] fetch(URL url, String userAgent) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Connection", "Close");
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setInstanceFollowRedirects(true);
        final int stat = connection.getResponseCode();
        byte[] contents = null;
        if (stat == HttpURLConnection.HTTP_OK) {
            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            if (isChunkedContent(connection)) {
                contents = getChunkedContent(bis);
            } else {
                contents = getContent(bis, connection.getContentLength());
            }
            bis.close();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("'%s' : [%d] - %d", url.toString(), stat, contents.length));
            }
        } else {
            //Warning
            LOGGER.warning(String.format("%s : %d", url.toString(), stat));
        }
        connection.disconnect();
        if( null == contents) {
            throw new IOException("HTTP error : " + stat);
        }
        return contents;
    }

    private static boolean isChunkedContent(HttpURLConnection connection) {
        return "Chunked".equalsIgnoreCase(connection.getHeaderField("Transfer-Encoding"));
    }

    private static byte[] getChunkedContent(InputStream in) throws IOException {
        ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len = 0;
        while((len = in.read(buffer)) > 0 ) {
            contentBuffer.write(buffer, 0, len);
        }
        return contentBuffer.toByteArray();
    }

    private static byte[] getContent(InputStream in, int contentLength) throws IOException {
        ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();
        byte readBuf[] = new byte[contentLength];
        int readLen = 0;
        while((readLen = in.read(readBuf)) > 0 ) {
            contentBuffer.write(readBuf, 0, readLen);
            contentBuffer.flush();
        }
        return contentBuffer.toByteArray();
    }

}
