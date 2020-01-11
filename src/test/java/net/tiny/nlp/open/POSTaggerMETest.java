package net.tiny.nlp.open;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import opennlp.tools.chunker.ChunkSample;
import opennlp.tools.chunker.ChunkSampleStream;
import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerFactory;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.WordTagSampleStream;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelType;

/**
 *
 * DT(Determiner)
 * NN (Noun, singular or mass)
 * VBD (Verb, past tense)
 * RB (Adverb )
 * VBN (Verb, past participle)
 *
 * @see https://blog.csdn.net/weixin_34185560/article/details/88849054
 * @see https://github.com/Ailab403/ailab-mltk4j
 */
public class POSTaggerMETest {

       @Test
        public void testPOSTagger() throws Exception {
           Path coprusFile = Paths.get("src/test/resources/train/pos-tagger.txt");

           TrainingParameters params = new TrainingParameters();
            params.put(TrainingParameters.ALGORITHM_PARAM, ModelType.MAXENT.toString());
            params.put(TrainingParameters.ITERATIONS_PARAM, 100);
            params.put(TrainingParameters.CUTOFF_PARAM, 5);
            InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(coprusFile.toFile());
            ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
            ObjectStream<POSSample> sampleStream = new WordTagSampleStream(lineStream);

            POSModel posModel = POSTaggerME.train("eng", sampleStream, params, new POSTaggerFactory());
            POSTagger tagger = new POSTaggerME(posModel);

            String[] tags = tagger.tag(new String[] {
                    "The",
                    "driver",
                    "got",
                    "badly",
                    "injured",
                    "."});

            assertEquals(6, tags.length);
            for( String t : tags) {
                System.out.println(" " + t);
            }
            /*
            assertEquals("DT", tags[0]);
            assertEquals("NN", tags[1]);
            assertEquals("VBD", tags[2]);
            assertEquals("RB", tags[3]);
            assertEquals("VBN", tags[4]);
            assertEquals(".", tags[5]);
            */
        }


       @Test
       public void testChunkAsArray() throws Exception {
           //B 标注开始
           //I 标注的中间
           //E 标注的结束
           //NP 名词块
           //VB 动词块
           Path chunkFile = Paths.get("src/test/resources/train/chunker.txt");
           InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(chunkFile.toFile());
           ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
           ObjectStream<ChunkSample> sampleStream = new ChunkSampleStream(lineStream);

           TrainingParameters params = new TrainingParameters();
           params.put(TrainingParameters.ITERATIONS_PARAM, 70);
           params.put(TrainingParameters.CUTOFF_PARAM, 1);

           ChunkerModel chunkerModel = ChunkerME.train("eng", sampleStream, params, new ChunkerFactory());

           Chunker chunker = new ChunkerME(chunkerModel);

           String sent[] = new String[] { "Rockwell", "International", "Corp.", "'s",
                    "Tulsa", "unit", "said", "it", "signed", "a", "tentative", "agreement",
                    "extending", "its", "contract", "with", "Boeing", "Co.", "to",
                    "provide", "structural", "parts", "for", "Boeing", "'s", "747",
                    "jetliners", "." };

            String pos[] = new String[] { "NNP", "NNP", "NNP", "POS", "NNP", "NN",
                "VBD", "PRP", "VBD", "DT", "JJ", "NN", "VBG", "PRP$", "NN", "IN",
                "NNP", "NNP", "TO", "VB", "JJ", "NNS", "IN", "NNP", "POS", "CD", "NNS",
                "." };
           String[] preds = chunker.chunk(sent, pos);
           for( String t : preds) {
               System.out.println(" " + t);
           }

           String[] expect1 = { "B-NP", "B-VP", "B-NP", "I-NP", "B-VP", "B-SBAR",
                   "B-NP", "B-VP", "I-VP", "B-NP", "I-NP", "I-NP", "I-NP", "B-PP", "B-NP",
                   "I-NP", "O" };
           //assertArrayEquals(expect1, preds);
       }

}
