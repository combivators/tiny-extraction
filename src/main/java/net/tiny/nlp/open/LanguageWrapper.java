package net.tiny.nlp.open;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;

final class LanguageWrapper {

    private final SortedMap<String, String> languages;
    private final LanguageDetector detector;

    LanguageWrapper() {
        this(new Properties());
    }

    LanguageWrapper(Properties param) {
        try {
            String res = OpenNLP.getLanguageMapper(param);
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL mapped = loader.getResource(res);
            if (null == mapped) {
                throw new FileNotFoundException(res);
            }
            res = OpenNLP.getLanguageDetect(param);
            URL model = loader.getResource(res);
            if (null == model) {
                throw new FileNotFoundException(res);
            }
            this.languages = loadCodes(mapped);
            Collections.unmodifiableSortedMap(languages);
            this.detector = createLanguageDetector(model);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public int size() {
        return languages.size();
    }

    public String[] getCodes() {
        return languages.keySet().toArray(new String[languages.size()]);
    }

    public String getLanguage(String code) {
        if (null == code || code.isEmpty())
            return null;
        return languages.get(code);
    }

    public String guest(String text) {
        Language best = guestLanguage(text);
        if (null == best)
            return null;
        return getLanguage(best.getLang());
    }

    public Language guestLanguage(String input) {
        if (null == input || input.isEmpty())
            return null;
        return detector.predictLanguage(input);
    }

    public Language[] predictLanguages(String input) {
        if (null == input || input.isEmpty())
            return null;
        return detector.predictLanguages(input);
    }

    private LanguageDetector createLanguageDetector(URL resource) throws IOException {
        LanguageDetectorModel model = new LanguageDetectorModel(resource.openStream());
        return new LanguageDetectorME(model);
    }

    private SortedMap<String, String> loadCodes(URL resource) throws IOException {
        TreeMap<String, String> map = new TreeMap<>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resource.openStream()));
            String line = "";

            while ((line = reader.readLine()) != null) {
                String parts[] = line.split("\t");
                map.put(parts[0], parts[1]);
            }
            return map;
        } finally {
            if (null != reader)
                reader.close();
        }
    }
}
