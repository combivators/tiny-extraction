package net.tiny.nlp.open;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import opennlp.tools.langdetect.*;
import opennlp.tools.util.*;

/**
 * Language Detector Example in Apache OpenNLP
 * @see https://www.tutorialkart.com/opennlp/language-detector-example-in-apache-opennlp/
 */
public class LanguageDetectorMEExample {

    private static LanguageDetectorModel model;

    public static void main(String[] args) throws Exception {

        Path trainFile = Paths.get("src/test/resources/train/DoccatSample.txt");
        Path modelFile  = Paths.get("src/test/resources/models/custom-lang-detect.bin");
        //Clear model file
        if (Files.exists(modelFile)) {
            Files.delete(modelFile);
        }

        // loading the training data to LanguageDetectorSampleStream
        LanguageDetectorSampleStream sampleStream = null;
        InputStreamFactory dataIn = new MarkableFileInputStreamFactory(trainFile.toFile());
        ObjectStream lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
        sampleStream = new LanguageDetectorSampleStream(lineStream);
        // training parameters
        TrainingParameters params = new TrainingParameters();
        params.put(TrainingParameters.ITERATIONS_PARAM, 100);
        params.put(TrainingParameters.CUTOFF_PARAM, 5);
        params.put("DataIndexer", "TwoPass");
        params.put(TrainingParameters.ALGORITHM_PARAM, "NAIVEBAYES");
        // train the model
        try {
            model = LanguageDetectorME.train(sampleStream, params, new LanguageDetectorFactory());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Completed");
        // load the model
        LanguageDetector ld = new LanguageDetectorME(model);
        // use model for predicting the language
        Language[] languages = ld.predictLanguages("estava em uma marcenaria na Rua Bruno");
        System.out.println("Predicted languages..");
        for(Language language:languages){
            // printing the language and the confidence score for the test data to belong to the language
            System.out.println(language.getLang()+"  confidence:"+language.getConfidence());
        }
    }
}
