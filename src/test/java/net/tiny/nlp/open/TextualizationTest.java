package net.tiny.nlp.open;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class TextualizationTest {

    @Test
    public void testToText() throws Exception {
        String content = "<!DOCTYPE html>\r\n<html>\r\n<body>\r\n<p>This text is  normal.</p>\r\n<p><b>This   text is bold.</b></p>\r\n</body>\r\n</html>";
        String text = Textualization.text(content);
        assertEquals("This text is normal.\r\nThis text is bold.\r\n", text);
    }

    @Test
    public void testURLEncodeDecode() throws Exception {
        String url = "https://dic.nicovideo.jp/a/日本人の名前一覧";
        url = new URL(url).toString();
        assertEquals("https://dic.nicovideo.jp/a/日本人の名前一覧", url);
        url = new URI(url).toASCIIString();
        assertEquals("https://dic.nicovideo.jp/a/%E6%97%A5%E6%9C%AC%E4%BA%BA%E3%81%AE%E5%90%8D%E5%89%8D%E4%B8%80%E8%A6%A7", url);

        url = "https://dic.nicovideo.jp/a/%E6%97%A5%E6%9C%AC%E3%81%AE%E8%8B%97%E5%AD%97%28%E5%90%8D%E5%AD%97%29%E3%81%AE%E4%B8%80%E8%A6%A7";
        url = URLDecoder.decode(url, "UTF-8");
        assertEquals("https://dic.nicovideo.jp/a/日本の苗字(名字)の一覧", url);
    }

    @Test
    public void testJapneseNameText() throws Exception {
        String url = "https://dic.nicovideo.jp/a/日本人の名前一覧";
        String content = Textualization.fetch(url);
        String text = Textualization.text(content);
        System.out.print(text);

    }

    @Test
    public void testJapneseFamilyNameToText() throws Exception {
        URL url = new URL(new URI("https://dic.nicovideo.jp/a/日本の苗字(名字)の一覧").toASCIIString());
        System.out.print(Textualization.text(url));
    }

    @Test
    public void testGlobalNameToText() throws Exception {
        URL url = new URL(new URI("https://dic.nicovideo.jp/a/海外の姓名の一覧").toASCIIString());
        System.out.print(Textualization.text(url));
    }

    @Test
    public void testHtmlArticleToText() throws Exception {
        // 宮沢賢治:注文の多い料理店
        URL url = new URL(new URI("https://www.aozora.gr.jp/cards/000081/files/43754_17659.html").toASCIIString());
        System.out.print(Textualization.text(url, "Shift_JIS"));
    }

    @Test
    public void testCheckJapaneseText() throws Exception {
        // 平仮名4つ
        assertTrue( "ひらがな".matches( "\\p{InHiragana}{4}" ) );
        // カタカナ4つ
        assertTrue( "カタカナ".matches( "\\p{InKatakana}{4}" ) );
        // 漢字2つ
        assertTrue( "漢字".matches( "\\p{InCjkUnifiedIdeographs}{2}" ) );
    }

    @Test
    public void testJapneseFamilyName() throws Exception {
        Path path = Paths.get("src/test/resources/train/ja-family-name.txt");

        // 石切山[いしきりやま] > 石切山
        // 諫山（諌山）[いさやま]  > 諫山\r\n諌山
        // 河辺（河邊・河邉）  > 河辺\r\n河邊\r\n河邉
        Function<String,String> mapper = new Mapper();

        Predicate<String> filter = new Filter();

        Files.readAllLines(path)
            .stream()
            .filter(filter)
            .map(mapper)
            .forEach(s -> System.out.println(s));
    }

    @Test
    public void testJapneseFirshName() throws Exception {
        Path path = Paths.get("src/test/resources/train/ja-first-name.txt");

        // 石切山（いしきりやま） > 石切山
        Function<String,String> mapper = new Cutter();

        Predicate<String> filter = new Filter(new String[]{"^[ ].*"}, new String[]{"^[ ].*行$"});

        Files.readAllLines(path)
            .stream()
            .filter(filter)
            .map(mapper)
            .forEach(s -> System.out.println(s));
    }

    @Test
    public void testGlobalName() throws Exception {
        Path path = Paths.get("src/test/resources/train/ja-global-name.txt");

        // 石切山（いしきりやま） > 石切山
        // ベロニカ/ヴェロニカ（Veronica） > ベロニカ\r\nヴェロニカ
        // 予（豫）> 予\r\n豫
        // ウー（呉、武） > ウー\r\n呉\r\n武
        Function<String,String> mapper = new GlobalMapper();

        Predicate<String> filter = new Filter(new String[]{"^[ ].*"}, new String[]{"^[ ].*行$", "^[ ][\\u3040-\\u309F]$", "^[ ][\\[].*[\\]]$", ".*その他.*", });
        Files.readAllLines(path)
            .stream()
            .filter(filter)
            .map(mapper)
            .forEach(s -> System.out.println(s));
    }

    @Test
    public void testMapper() throws Exception {
        Function<String,String> mapper = new Mapper();
        assertEquals("石切山", mapper.apply("石切山[いしきりやま]"));
        assertEquals("諫山\r\n諌山", mapper.apply("諫山（諌山）[いさやま]"));
        assertEquals("河辺\r\n河邊\r\n河邉", mapper.apply("河辺（河邊・河邉）"));
    }

    @Test
    public void testGlobalMapper() throws Exception {
        Function<String,String> mapper = new GlobalMapper();
        assertEquals("石切山", mapper.apply("石切山（いしきりやま）"));
        assertEquals("ベロニカ\r\nヴェロニカ", mapper.apply("ベロニカ/ヴェロニカ（Veronica）"));
        assertEquals("予\r\n豫", mapper.apply("予（豫）"));
        assertEquals("ウー\r\n呉\r\n武", mapper.apply("ウー（呉、武）"));
    }
    // 石切山（いしきりやま） > 石切山
    // ベロニカ/ヴェロニカ（Veronica） > ベロニカ\r\nヴェロニカ
    // 予（豫）> 予\r\n豫
    // ウー（呉、武） > ウー\r\n呉\r\n武
    static class GlobalMapper implements Function<String,String> {
        @Override
        public String apply(String t) {
            t = t.trim();
            int s = t.indexOf("（");
            int e = t.indexOf("）");
            String alternate = null;
            if (s > 0 && e > s) {
                alternate = t.substring(s+1, e); //候补漢字
                if (!Pattern.matches("\\p{InCjkUnifiedIdeographs}+?", alternate)) {
                    String[] array = alternate.split("、");
                    alternate = "";
                    for (String a : array) {
                        if (Pattern.matches("\\p{InCjkUnifiedIdeographs}+?", a)) {
                            if (!alternate.isEmpty()) {
                                alternate = alternate.concat("\r\n");
                            }
                            alternate = alternate.concat(a);
                        }
                    }
                }
                t = t.substring(0, s);
            }
            t = t.replaceAll("/", "\r\n");
            if (null != alternate && !alternate.isEmpty()) {
                t = t.concat("\r\n").concat(alternate);
            }
            return t;
        }

    }

    // 石切山（いしきりやま） > 石切山
    static class Cutter implements Function<String,String> {
        @Override
        public String apply(String t) {
            t = t.trim();
            int s = t.indexOf("（");
            int e = t.indexOf("）");
            if (s > 0 && e > s) {
                t = t.substring(0, s);
            }
            return t;
        }
    }

    // 石切山[いしきりやま] > 石切山
    // 諫山（諌山）[いさやま]  > 諫山\r\n諌山
    // 河辺（河邊・河邉）  > 河辺\r\n河邊\r\n河邉
    static class Mapper implements Function<String,String> {
        @Override
        public String apply(String t) {
            t = t.trim();
            int s = t.indexOf("[");
            int e = t.indexOf("]");
            if (s > 0 && e > s) {
                t = t.substring(0, s);
            }
            s = t.indexOf("（");
            e = t.indexOf("）");
            if (s > 0 && e > s) {
                String[] array = t.substring(s+1, e).split("・");
                t  = t.substring(0, s);
                for (String a : array) {
                    t = t.concat("\r\n").concat(a);
                }
            }
            return t;
        }
    }

    @Test
    public void testFilter() throws Exception {
        Predicate<String> filter = new Filter();
        assertTrue(filter.test(" 石切山[いしきりやま]"));
        assertTrue(filter.test(" うえ"));
        assertFalse(filter.test("  あ / い / う / え / お"));
        assertFalse(filter.test(" あ行"));
        assertFalse(filter.test(" あ"));
    }

    static class Filter implements Predicate<String> {
        //ひらがな "^[\\u3040-\\u309F]+$"
        //カタカナ "^[\\u30A0-\\u30FF]+$"
        //一桁ひらがな "^[\\u3040-\\u309F]$"
        String[] includes = {"^[ ].*"};
        String[] excludes = {"^[ ].*行$", ".*[/].*", "^[ ][\\u3040-\\u309F]$"};
        Filter() {}

        Filter(String[] in, String[] ex) {
            includes = in;
            excludes = ex;
        }

        @Override
        public boolean test(String t) {
            for (String regx : includes) {
                if (!Pattern.matches(regx, t)) {
                    return false;
                }
            }
            for (String regx : excludes) {
                if (Pattern.matches(regx, t)) {
                    return false;
                }
            }
            return true;
        }
    }

}
