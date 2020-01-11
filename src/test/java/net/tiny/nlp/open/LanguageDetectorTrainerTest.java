package net.tiny.nlp.open;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorFactory;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;
import opennlp.tools.langdetect.LanguageDetectorSampleStream;
import opennlp.tools.langdetect.LanguageSample;
import opennlp.tools.ml.perceptron.PerceptronTrainer;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;

public class LanguageDetectorTrainerTest {

    @Disabled
    //@Test
    public void testLanguageTraining() throws Exception {
        Path coprusFile = Paths.get("src/test/resources/train/language-corpus.txt");
        Path modelFile  = Paths.get("src/test/resources/models/langdetect.bin");
        //Clear model file
        if (Files.exists(modelFile)) {
            Files.delete(modelFile);
        }

        InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(coprusFile.toFile());

        ObjectStream<String> lineStream =
          new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
        ObjectStream<LanguageSample> sampleStream = new LanguageDetectorSampleStream(lineStream);

        TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
        params.put(TrainingParameters.ALGORITHM_PARAM,  PerceptronTrainer.PERCEPTRON_VALUE);
        params.put(TrainingParameters.CUTOFF_PARAM, 0);

        // TODO opennlp.tools.util.InsufficientTrainingDataException: Insufficient training data to create model.
        LanguageDetectorFactory factory = new LanguageDetectorFactory();

        LanguageDetectorModel model = LanguageDetectorME.train(sampleStream, params, factory);
        model.serialize(modelFile.toFile());
   }

}
