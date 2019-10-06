package net.tiny.nlp.open;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;

public class NERTrainerTest {


    @Test
    public void testPersonTraining() throws Exception {
        Path coprusFile = Paths.get("src/test/resources/train/AnnotatedSentences.txt");
        Path modelFile  = Paths.get("src/test/resources/models/en-ner-custom.bin");
        //Clear model file
        if (Files.exists(modelFile)) {
            Files.delete(modelFile);
        }
        //String entity = "person";
        String entity = null;
        NERTrainer trainer = new NERTrainer();
        TokenNameFinder nameFinder = trainer.train("en", entity, coprusFile, modelFile);

        assertTrue(Files.exists(modelFile));

        // Testing the model and printing the types it found in the input sentence
        String text = "Alisa Fernandes is a tourist from Spain";
        String[] sentence = text.split("\\s+");
        System.out.println("Finding types in the test sentence..");

        //OpenNLP.appendModels("models/en-ner-custom.bin");
        //OpenNLP.print("en", "custom", sentence, System.out);

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
    public void testPlaceTraining() throws Exception {
        //Path coprusFile = Paths.get("src/test/resources/train/AnnotatedSentencesWithTypes.txt");
        Path coprusFile = Paths.get("src/test/resources/train/AnnotatedSentences.txt");
        Path modelFile  = Paths.get("src/test/resources/models/en-ner-custom.bin");
        //Clear model file
        if (Files.exists(modelFile)) {
            Files.delete(modelFile);
        }
        String entity = "place";
        NERTrainer trainer = new NERTrainer();
        TokenNameFinder nameFinder = trainer.train("en", entity, coprusFile, modelFile);

        assertTrue(Files.exists(modelFile));

        // Testing the model and printing the types it found in the input sentence
        String text = "Near the Ostbahnhof in 56473 Hamburg.";
        String[] sentence = text.split("\\s+");
        System.out.println("Finding types in the test sentence..");

        //OpenNLP.appendModels("models/en-ner-custom.bin");
        //OpenNLP.print("en", "custom", sentence, System.out);

        Span[] names = nameFinder.find(sentence);
        for (Span name : names) {
            String personName = "";
            for (int i = name.getStart(); i < name.getEnd(); i++) {
                personName += sentence[i] + " ";
            }
            System.out.println(name.getType() + " : " + personName + "\t [probability=" + name.getProb() + "]");
        }

    }


    // Create a NameSampleDataStream from a corpus with entities annotated
    // with multiple nameTypes, like person, date, location and organization, and validate it.
    /*
    @Test
    public void testWithNameTypes() throws Exception {
      InputStream in = getClass().getClassLoader().getResourceAsStream(
          "opennlp/tools/namefind/voa1.train");

      NameSampleDataStream ds = new NameSampleDataStream(
          new PlainTextByLineStream(new InputStreamReader(in)));

      int person = 14;
      int date = 3;
      int location = 17;
      int organization = 1;

      NameSample ns = ds.read();

      Map<String, List<String>> names = new HashMap<String, List<String>>();

      while (ns != null) {
        Span[] nameSpans = ns.getNames();
        String[] types = ns.getNameTypes();

        for (int i = 0; i < nameSpans.length; i++) {
          if (!names.containsKey(types[i])) {
            names.put(types[i], new ArrayList<String>());
          }
          names.get(types[i])
              .add(sublistToString(ns.getSentence(), nameSpans[i]));
        }

        ns = ds.read();
      }

      for (String type : names.keySet()) {
        System.out.println("Type: " + type + " " + names.get(type).size());
        for (String name : names.get(type)) {
          System.out.println("\"" + name + "\",");
        }
      }

      assertEquals(person, names.get("person").size());
      assertEquals(date, names.get("date").size());
      assertEquals(location, names.get("location").size());
      assertEquals(organization, names.get("organization").size());
    }
    */
}
