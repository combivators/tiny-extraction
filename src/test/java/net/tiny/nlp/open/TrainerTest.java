package net.tiny.nlp.open;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.util.Span;

public class TrainerTest {

    @Test
    public void testTrainChunkerModel() throws Exception {
        Path trainFile = Paths.get("src/test/resources/train/en-chunker.train");
        Path modelFile  = Paths.get("src/test/resources/models/en-chunker.bin");
        //Clear model file
        if (Files.exists(modelFile)) {
            Files.delete(modelFile);
        }
        Chunker chunker = Trainer.trainChunkerModel("en", trainFile, modelFile);
        assertNotNull(chunker);

    }

    @Test
    public void testSimpleChunkerModel() throws Exception {
        Path trainFile = Paths.get("src/test/resources/train/chunker2.txt");
        Path modelFile  = Paths.get("src/test/resources/models/en-chunker.bin");
        //Clear model file
        if (Files.exists(modelFile)) {
            Files.delete(modelFile);
        }
        Chunker chunker = Trainer.trainChunkerModel("en", trainFile, modelFile);
        assertNotNull(chunker);

        String[] toks1 = { "Rockwell", "said", "the", "agreement", "calls", "for",
                "it", "to", "supply", "200", "additional", "so-called", "shipsets",
                "for", "the", "planes", "." };

        String[] tags1 = { "NNP", "VBD", "DT", "NN", "VBZ", "IN", "PRP", "TO", "VB",
                "CD", "JJ", "JJ", "NNS", "IN", "DT", "NNS", "." };
        String[] preds = chunker.chunk(toks1, tags1);
        for( String t : preds) {
            System.out.println(" " + t);
        }
    }


    @Test
    public void testTokenNameFinderEvaluator() throws Exception {
        Path trainFile = Paths.get("src/test/resources/train/ja-person.txt");
        Path modelFile  = Paths.get("src/test/resources/models/ja-ner-person.bin");
        //Clear model file
        if (Files.exists(modelFile)) {
            Files.delete(modelFile);
        }
        TokenNameFinder nameFinder = Trainer.trainNER("ja", "Name", trainFile, modelFile);

        String text = "渡辺 由美子 先生 は、 28 歳 に こ の 大学 を 卒業 しました";
        String[] sentence = text.split("\\s+");
        System.out.println("Finding types in the test sentence..");
        Span[] names = nameFinder.find(sentence);
        for (Span name : names) {
            String personName = "";
            for (int i = name.getStart(); i < name.getEnd(); i++) {
                personName += sentence[i] + " ";
            }
            System.out.println(name.getType() + " : " + personName + "\t [probability=" + name.getProb() + "]");
        }
    }

    @Test
    public void testTokenNameFinderDate() throws Exception {
        Path trainFile = Paths.get("src/test/resources/train/ja-date.txt");
        Path modelFile  = Paths.get("src/test/resources/models/ja-ner-date.bin");
        //Clear model file
        if (Files.exists(modelFile)) {
            Files.delete(modelFile);
        }
        TokenNameFinder nameFinder = Trainer.trainNER("ja", "Date", trainFile, modelFile);

        String text = "渡辺 由美子 先生 は、 2005 年 12 月 に こ の 大学 を 卒業 しました";
        String[] sentence = text.split("\\s+");
        System.out.println("Finding types in the test sentence..");
        Span[] names = nameFinder.find(sentence);
        for (Span name : names) {
            String personName = "";
            for (int i = name.getStart(); i < name.getEnd(); i++) {
                personName += sentence[i] + " ";
            }
            System.out.println(name.getType() + " : " + personName + "\t [probability=" + name.getProb() + "]");
        }

        text = "宮沢 賢治 さん の 小説 「 注文 の 多い料理店 」 は、 昭和 44 年 4 月 14 日 出版 されました。";
        sentence = text.split("\\s+");
        System.out.println("Finding types in the test sentence..");
        names = nameFinder.find(sentence);
        for (Span name : names) {
            String personName = "";
            for (int i = name.getStart(); i < name.getEnd(); i++) {
                personName += sentence[i] + " ";
            }
            System.out.println(name.getType() + " : " + personName + "\t [probability=" + name.getProb() + "]");
        }
    }
}
