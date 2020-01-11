package net.tiny.nlp.open;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class SentenceDetectorFactory {
    private SentenceDetector detector;

    public SentenceDetectorFactory() throws IOException {
        this(new Properties());
    }
    public SentenceDetectorFactory(Properties param) throws IOException {
        this( OpenNLP.getModelPath(param), OpenNLP.getModelLanguage(param));
    }

    public SentenceDetectorFactory(String path, String language) throws IOException {
        loadSentenceDetector(path, language);
    }


    /**
     * Obtain a reference to an english sentence detector to use in conjunction with
     * the NameFinders
     */
    public SentenceDetector getSentenceDetector() {
        return detector;
    }

    /**
     * Load the sentence detector
     *
     * @param path
     * @param language
     *
     * @throws IOException
     */
    protected void loadSentenceDetector(String path, String language) throws IOException {
        String modelFile = String.format("%s/%s--sent.bin", path, language);
        InputStream modelStream = new FileInputStream(modelFile);
        SentenceModel model = new SentenceModel(modelStream);
        detector = new SentenceDetectorME(model);
    }
}
