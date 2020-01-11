package net.tiny.nlp.open;

import java.io.IOException;
import java.net.URL;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

/**
 *
 * This class demonstrates how to use NameFinderME class to do Named Entity Recognition/Extraction tasks.
 *
 * @see https://www.tutorialkart.com/opennlp/named-entity-extraction-example-opennlp/
 *
 */
public class NameFinderExample {
    public static void main(String[] args) {
        // find person name
        try {
            System.out.println("-------Finding entities belonging to category : person name------");
            new NameFinderExample().findName();
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // find place
        try {
            System.out.println("-------Finding entities belonging to category : place name------");
            new NameFinderExample().findLocation();
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * method to find locations in the sentence
     * @throws IOException
     */
    public void findName() throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("opennlp/models/en-ner-person.bin");


        // load the model from file
        TokenNameFinderModel model = new TokenNameFinderModel(url.openStream());

        // feed the model to name finder class
        NameFinderME nameFinder = new NameFinderME(model);

        // input string array
        String[] sentence = new String[]{
                "John",
                "Smith",
                "is",
                "standing",
                "next",
                "to",
                "bus",
                "stop",
                "and",
                "waiting",
                "for",
                "Mike",
                "."
        };

        Span nameSpans[] = nameFinder.find(sentence);

        // nameSpans contain all the possible entities detected
        for(Span s: nameSpans){
            System.out.print(s.toString());
            System.out.print("  :  ");
            // s.getStart() : contains the start index of possible name in the input string array
            // s.getEnd() : contains the end index of the possible name in the input string array
            for(int index=s.getStart();index<s.getEnd();index++){
                System.out.print(sentence[index]+" ");
            }
            System.out.println();
        }
    }

    /**
     * method to find locations in the sentence
     * @throws IOException
     */
    public void findLocation() throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("opennlp/models/en-ner-location.bin");

        // load the model from file
        TokenNameFinderModel model = new TokenNameFinderModel(url.openStream());

        // feed the model to name finder class
        NameFinderME nameFinder = new NameFinderME(model);

        // input string array
        String[] sentence = new String[]{
                "John",
                "Smith",
                "is",
                "from",
                "Atlanta",
                "."
        };

        Span nameSpans[] = nameFinder.find(sentence);

        // nameSpans contain all the possible entities detected
        for(Span s: nameSpans){
            System.out.print(s.toString());
            System.out.print("  :  ");
            // s.getStart() : contains the start index of possible name in the input string array
            // s.getEnd() : contains the end index of the possible name in the input string array
            for(int index=s.getStart();index<s.getEnd();index++){
                System.out.print(sentence[index]+" ");
            }
            System.out.println();
        }
    }
}
