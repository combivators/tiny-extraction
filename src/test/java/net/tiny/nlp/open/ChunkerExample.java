package net.tiny.nlp.open;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;

public class ChunkerExample {
    public static void main(String args[]) throws IOException {
        // Tokenizing the sentence
        String sentence = "Hi welcome to Tutorialspoint";
        WhitespaceTokenizer whitespaceTokenizer = WhitespaceTokenizer.INSTANCE;
        String[] tokens = whitespaceTokenizer.tokenize(sentence);

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("opennlp/models/en-pos-maxent.bin");

        // Generating the POS tags
        // Load the parts of speech model
        POSModel model = new POSModel(url.openStream());

        // Constructing the tagger
        POSTaggerME tagger = new POSTaggerME(model);

        // Generating tags from the tokens
        String[] tags = tagger.tag(tokens);

        // Loading the chunker model
        url = loader.getResource("opennlp/models/en-chunker.bin");
        ChunkerModel chunkerModel = new ChunkerModel(url.openStream());

        // Instantiate the ChunkerME class
        ChunkerME chunkerME = new ChunkerME(chunkerModel);

        // Generating the chunks
        String result[] = chunkerME.chunk(tokens, tags);

        for (String s : result) {
            System.out.println(s);
        }
    }
}
