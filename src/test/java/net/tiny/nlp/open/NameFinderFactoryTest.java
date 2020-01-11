package net.tiny.nlp.open;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import opennlp.tools.namefind.NameFinderME;

public class NameFinderFactoryTest {

    @Disabled
    //@Test
    public void testFindPatLanguage() throws Exception {
        // Load about 6s
        NameFinderFactory factory = new NameFinderFactory("../tiny-dic/src/main/resources/opennlp/models", "en");
        assertNotNull(factory);
        String[] models = factory.getAllModelNames();
        assertEquals(7, models.length);
        NameFinderME finder = factory.getNameFinders("en", "person");
        assertNotNull(finder);

        factory.clear();
    }


    @Test
    public void testFindFromResources() throws Exception {
        // Load about 6s
        NameFinderFactory factory = new NameFinderFactory();
        assertNotNull(factory);
        String[] models = factory.getAllModelNames();
        assertEquals(0, models.length);

        String res ="rondhuit/rondhuit-ja-ner-1.0.0.bin";
        factory = new NameFinderFactory(res); // Load about 500ms
        models = factory.getAllModelNames();
        assertEquals(1, models.length);
        assertEquals("ja-ner", models[0]);

        NameFinderME finder = factory.getNameFinders("ja", "name");
        assertNotNull(finder);

        factory.clear();
    }

    @Test
    public void testJapanesExtraction() throws Exception {
        String text = "故障 者 リスト 入り し て い た エンゼルス ・ 大谷 翔平 投手 （ ２ ３ ） が 戦列 復帰 。";
        String[] sentence = text.split("\\s+");
        String res ="rondhuit/rondhuit-ja-ner-1.0.0.bin";
        OpenNLP.appendModels(res);
        Extraction extraction = OpenNLP.extract("ja", "name", sentence);
        String[] types = extraction.types();
        assertEquals(2, types.length);
        assertEquals("エンゼルス ・ 大谷 翔平", extraction.findFirst("PERSON"));
        assertEquals("投手", extraction.findFirst("TITLE"));
    }

}
