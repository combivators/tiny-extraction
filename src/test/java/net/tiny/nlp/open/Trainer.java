package net.tiny.nlp.open;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import opennlp.tools.chunker.ChunkSample;
import opennlp.tools.chunker.ChunkSampleStream;
import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerFactory;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
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

public class Trainer {

    private Trainer() {}

    public static TokenNameFinder trainNER(String lang, String entity, Path coprusFile, Path modelFile) throws IOException {
        // Setting the parameters for training
        TrainingParameters params = new TrainingParameters();
        params.put(TrainingParameters.ITERATIONS_PARAM, 80);
        params.put(TrainingParameters.CUTOFF_PARAM, 1);
        return trainNER(lang, entity, new MarkableFileInputStreamFactory(coprusFile.toFile()), Files.newOutputStream(modelFile), params);
    }

    public static TokenNameFinder trainNER(String lang, String entity, InputStreamFactory corpusStream, OutputStream modelStream, TrainingParameters params) throws IOException {

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


    public static Chunker trainChunkerModel(String lang, Path trainFile, Path modelFile) throws IOException {
        return trainChunkerModel(lang, new MarkableFileInputStreamFactory(trainFile.toFile()), Files.newOutputStream(modelFile), TrainingParameters.defaultParams());
    }

    public static Chunker trainChunkerModel(String lang,
            InputStreamFactory corpusStream, OutputStream modelStream, TrainingParameters params)
            throws IOException {

        // Reading training data
        ObjectStream<ChunkSample> sampleStream = new ChunkSampleStream(
                new PlainTextByLineStream(corpusStream, StandardCharsets.UTF_8));
        OutputStream modelOut = null;
        try {
            ChunkerModel model = ChunkerME.train(lang, sampleStream, params, new ChunkerFactory());
            modelOut = new BufferedOutputStream(modelStream);
            model.serialize(modelOut);
            return new ChunkerME(model);
        } finally {
            sampleStream.close();
            if (null != modelOut) {
                modelOut.close();
            }
        }
    }
}
