package net.tiny.nlp.open;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import opennlp.tools.namefind.BioCodec;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.featuregen.AggregatedFeatureGenerator;
import opennlp.tools.util.featuregen.PreviousMapFeatureGenerator;
import opennlp.tools.util.featuregen.TokenClassFeatureGenerator;
import opennlp.tools.util.featuregen.TokenFeatureGenerator;
import opennlp.tools.util.featuregen.WindowFeatureGenerator;

/**
 * NER Training in OpenNLP with Name Finder Training Java class
 *
 * @see https://opennlp.apache.org/docs/1.9.1/manual/opennlp.html
 * @see https://www.tutorialkart.com/opennlp/ner-training-in-opennlp-with-name-finder-training-java-example/
 */
public class NERTrainer {

    // 默认参数
    private int iterations = 80;
    private int cutoff = 1;

    public TokenNameFinder train(String lang, String entity, Path coprusFile, Path modelFile) throws IOException {
        return train(lang, entity, new MarkableFileInputStreamFactory(coprusFile.toFile()), Files.newOutputStream(modelFile));
    }

    public TokenNameFinder train(String lang, String entity, InputStreamFactory corpusStream, OutputStream modelStream) throws IOException {
        // Setting the parameters for training
        TrainingParameters params = new TrainingParameters();
        params.put(TrainingParameters.ITERATIONS_PARAM, iterations);
        params.put(TrainingParameters.CUTOFF_PARAM, cutoff);


        OutputStream modelOut = null;

        // Reading training data
        ObjectStream<NameSample> sampleStream = new NameSampleDataStream(
                new PlainTextByLineStream(corpusStream, StandardCharsets.UTF_8));

        try {
            TokenNameFinderFactory factory = TokenNameFinderFactory.create(null, null, Collections.emptyMap(), new BioCodec());
            // Training the model using TokenNameFinderModel class
            TokenNameFinderModel model  = NameFinderME.train(lang, entity, sampleStream, params, factory);

            // Saving the model to a file
            modelOut = new BufferedOutputStream(modelStream);
            if (model != null) {
                model.serialize(modelOut);
            }

            // Return Testing the model
            return new NameFinderME(model);
        } finally {
            sampleStream.close();
            if (modelOut != null) {
                modelOut.close();
            }
        }
    }

    /**
     * 生成定制特征
     *
     * @return
     */
    public AggregatedFeatureGenerator prodFeatureGenerators() {
        AggregatedFeatureGenerator featureGenerators = new AggregatedFeatureGenerator(
                new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
                new WindowFeatureGenerator(new TokenClassFeatureGenerator(), 2, 2),
                new PreviousMapFeatureGenerator());
        return featureGenerators;
    }
}
