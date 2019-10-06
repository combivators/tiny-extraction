package net.tiny.nlp.open;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.junit.jupiter.api.Test;

public class OpenNLPTest {

    @Test
    public void testGetModelDirectory() throws Exception {
        Properties args = null;
        // Get default path
        String path = OpenNLP.getModelPath(args);
        assertEquals("opennlp/models", path);

        args = new Properties();
        args.put(OpenNLP.MODEL_PATH, "trained/model");
        path = OpenNLP.getModelPath(args);
        assertEquals("trained/model", path);
    }

    @Test
    public void testGetModelLanguage() throws Exception {
        Properties args = null;
        // Get default language
        String lang = OpenNLP.getModelLanguage(args);
        assertEquals("en", lang);

        args = new Properties();
        args.put(OpenNLP.MODEL_LANGUAGE, "ja");
        lang = OpenNLP.getModelLanguage(args);
        assertEquals("ja", lang);
    }

    @Test
    public void testGetModelResources() throws Exception {
        Properties args = null;
        String[] res = OpenNLP.getModelResources(args);
        assertEquals(0, res.length);

        args = new Properties();
        args.put(OpenNLP.MODEL_RESOURCES, "ja, zh,en");
        res = OpenNLP.getModelResources(args);
        assertEquals(3, res.length);
        assertEquals("ja", res[0]);
        assertEquals("zh", res[1]);
        assertEquals("en", res[2]);

    }


    @Test
    public void testFindModelResource() throws Exception {
        Properties args = null;
        // Get default path
        String path = OpenNLP.getModelPath(args);
        assertEquals("opennlp/models", path);
        // Get default language
        String language = OpenNLP.getModelLanguage(args);
        assertEquals("en", language);
        String type = "date";
        final String model = String.format("%s/%s-ner-%s.bin", path, language, type);
        assertEquals("opennlp/models/en-ner-date.bin", model);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(model);
        assertNotNull(url);
    }

    @Test
    public void testGetModelName() throws Exception {
        assertEquals("en-ner-name", OpenNLP.getModelName("en", "name"));
        assertEquals("ja-ner-organization", OpenNLP.getModelName("ja", "organization"));
        assertEquals("ja-ner", OpenNLP.getModelName("ja", null));
    }

    @Test
    public void testGuestModelName() throws Exception {
        File file = new File("src/test/resources/sample/models/en-ner-date.bin");
        URL url = file.toURI().toURL();
        assertEquals("en-ner-date", OpenNLP.guestModelName(url));
        assertEquals("zh-ner-person", OpenNLP.guestModelName(new URL("http://localhost:8080/v1/api/nlp/model/zh/ner/person")));
        assertEquals("ja-ner", OpenNLP.guestModelName(new URL("http://localhost:8080/v1/api/nlp/model/ja/ner")));
        assertEquals("zh-ner", OpenNLP.guestModelName(new URL("http://localhost:8080/opennlp/models/zh-ner.bin")));
        assertEquals("ja-ner", OpenNLP.guestModelName(new URL("http://localhost:8080/rondhuit/rondhuit-ja-ner-1.0.0.bin")));
    }

    @Test
    public void testJapanesExtraction() throws Exception {
        //String text = "故障 者 リスト 入り し て い た エンゼルス ・ 大谷 翔平 投手 （ ２ ３ ） が 戦列 復帰 。";
        String text = "大谷 翔平 投手 （ ２３ ） が 戦列 復帰 。";
        String[] sentence = text.split("\\s+");
        //加载日语模型文件 参照 tiny-dic
        OpenNLP.appendModels("rondhuit/rondhuit-ja-ner-1.0.0.bin");
        OpenNLP.parse("ja", "name", sentence, System.out);
        OpenNLP.parse("ja", "name", new String[] {"大谷翔平"}, System.out);
        OpenNLP.parse("ja", "name", new String[] {"大谷", "翔平"}, System.out);
        OpenNLP.parse("ja", "name", new String[] {"前澤", "友作", "氏"}, System.out);
        OpenNLP.parse("ja", "name", new String[] {"CEO", "川邊", "健太郎", "氏"}, System.out);
        OpenNLP.parse("ja", "name", new String[] {"楽天", "を", "追い", "かけたい", "ヤフー"}, System.out);
    }

    @Test
    public void testGuestLanguage() throws Exception {
        // Get the most probable language
        assertEquals("English", OpenNLP.guestLanguage("English Japanese Mandarin Chinese"));
        assertEquals("Japanese", OpenNLP.guestLanguage("お名前"));
        assertEquals("Mandarin Chinese", OpenNLP.guestLanguage("名前")); //??
        assertEquals("Mandarin Chinese", OpenNLP.guestLanguage("姓名"));
        assertEquals("Spanish", OpenNLP.guestLanguage("A la fecha tres calles bonaerenses recuerdan su nombre (en Ituzaingó, Merlo y Campana)."));
        assertEquals("Standard Latvian", OpenNLP.guestLanguage("Egija Tri-Active procedūru īpaši iesaka izmantot siltākajos gadalaikos"));
        assertEquals("Serbian", OpenNLP.guestLanguage("Већина становника боравила је кућама од блата или шаторима"));
        assertEquals("German", OpenNLP.guestLanguage("Alle Jahre wieder: Millionen Spanier haben am Dienstag die Auslosung in der größten Lotterie der Welt verfolgt"));
    }

    @Test
    public void testPredictLanguage() throws Exception {
        String[] langs = OpenNLP.predictLanguage("名前");
        assertTrue(langs.length >2); //langs.length = 103
        assertEquals("Mandarin Chinese", langs[0]);
        assertEquals("Japanese", langs[1]);
        assertEquals("Kirghiz", langs[2]);

        OpenNLP.predictLanguage("名前", System.out);

    }
}
