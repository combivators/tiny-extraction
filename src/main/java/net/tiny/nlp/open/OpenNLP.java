package net.tiny.nlp.open;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import opennlp.tools.langdetect.Language;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

/**
 * Load model resource take some time.
 *
 * @see https://opennlp.apache.org/docs/1.9.1/manual/opennlp.html#tools.langdetect
 *
 * {@link OpenNLP#guestLanguage(String)}
 *
 */
public class OpenNLP {

    private static final Logger LOGGER = Logger.getLogger(OpenNLP.class.getName());
    public static final String MODEL_RESOURCES = "model.resources";
    public static final String MODEL_PATH = "model.path";
    public static final String MODEL_LANGUAGE = "model.language";
    public static final String DEFAULT_MODEL_LANGUAGE = "en";
    public static final String DEFAULT_MODEL_PATH = "opennlp/models";

    public static final String LANGUAGE_MAPPER = "language.mapper";
    public static final String LANGUAGE_DETECT = "language.detect";
    public static final String DEFAULT_LANG_MAPPER        = DEFAULT_MODEL_PATH + "/language_mapping.txt";
    public static final String DEFAULT_LANG_DETECT_MODEL  = DEFAULT_MODEL_PATH + "/langdetect-183.bin";

    private static OpenNLP instance = new OpenNLP();

    private NameFinderFactory factory = new NameFinderFactory();
    private LanguageWrapper languageDetector = new LanguageWrapper();

    private OpenNLP() {
    }

    public static String getModelPath(Properties args) {
        return getModelParam(args, MODEL_PATH, DEFAULT_MODEL_PATH);
    }

    public static String getModelLanguage(Properties args) {
        return getModelParam(args, MODEL_LANGUAGE, DEFAULT_MODEL_LANGUAGE);
    }

    public static String getLanguageMapper(Properties args) {
        return getModelParam(args, LANGUAGE_MAPPER, DEFAULT_LANG_MAPPER);
    }

    public static String getLanguageDetect(Properties args) {
        return getModelParam(args, LANGUAGE_DETECT, DEFAULT_LANG_DETECT_MODEL);
    }

    public static String[] getModelResources(Properties args) {
        String param = "";
        if (args != null && !args.isEmpty()) {
            param = args.getProperty(MODEL_RESOURCES);
        }
        if (param == null || param.isEmpty()) {
            return new String[0];
        }
        return param.split("[ ]*,[ ]*");
    }

    static String getModelParam(Properties args, String key, String defaultValue) {
        String param = null;
        if (args != null && !args.isEmpty()) {
            param = args.getProperty(key);
        }

        if (param == null || param.isEmpty()) {
            param = System.getProperty(key);
        }

        if (param == null || param.isEmpty()) {
            LOGGER.fine(String.format("'%s' property not set, using default: %s", key, defaultValue));
            param = defaultValue;
        }
        return param;
    }

    public static NameFinderME loadNameFinders(String path, String language, String type) {
        final String model;
        if (null != type) {
            model = String.format("%s/%s-ner-%s.bin", path, language, type);
        } else {
            model = String.format("%s/%s-ner.bin", path, language);
        }
        return loadNameFinders(Thread.currentThread().getContextClassLoader().getResource(model));
    }

    public static NameFinderME loadNameFinders(URL url) {
        if (null == url)
            return null;
        try {
            // 返回创建的NameFinderME实例
            return new NameFinderME(new TokenNameFinderModel(url.openStream()));
        } catch (IOException e) {
            return null;
        }
    }

    public static String guestModelName(URL url) {
        String res = url.getPath();
        String[] array = res.split("[^a-zA-Z]");
        int pos = array.length;
        while (!"ner".equals(array[--pos]) && pos > 1)
            ;
        if (pos == (array.length - 1) || "bin".equals(array[pos + 1]) || array[pos + 1].isEmpty()) {
            return String.format("%s-ner", array[pos - 1]);
        }
        return String.format("%s-ner-%s", array[pos - 1], array[pos + 1]);
    }

    public static String getModelName(String language, String type) {
        final String modelName;
        if (null != type) {
            modelName = String.format("%s-ner-%s", language, type);
        } else {
            modelName = String.format("%s-ner", language);
        }
        return modelName;
    }

    public static void appendModels(String... resources) {
        instance.factory.concat(new NameFinderFactory(resources));
    }

    public static Extraction extract(String language, String type, String[] sentence) {
        if (!hasFinder()) {
            throw new IllegalStateException("Not found a OpenNLP NameFinder.");
        }
        final NameFinderME finder = instance.factory.getNameFinders(language, type);
        // 给定一个被分成单词的句子，提取命名表达式
        final Span[] spans = finder.find(sentence);
        final Extraction extraction = new Extraction();
        for (Span span : spans) {
            extraction.append(span.getType(), span2string(span, sentence));
        }
        return extraction;
    }

    static boolean hasFinder() {
        return !instance.factory.isEmpty();
    }
    static String span2string(Span span, String[] sentence) {
        StringBuilder sb = new StringBuilder();
        for (int i = span.getStart(); i < span.getEnd(); i++) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(sentence[i]);
        }
        return sb.toString();
    }

    public static void parse(String language, String type, String[] sentence, PrintStream out) {
        if (!hasFinder()) {
            throw new IllegalStateException("Not found a OpenNLP NameFinder.");
        }
        final NameFinderME finder = instance.factory.getNameFinders(language, type);
        // 给定一个被分成单词的句子，提取命名表达式
        final Span[] spans = finder.find(sentence);
        for (Span span : spans) {
            out.printf("Span(%d,%d,%s[%.4f])=\"%s\"\n", span.getStart(), span.getEnd(),
                    span.getType(),	span.getProb(), span2string(span, sentence));
        }
    }

    public static String[] getLanguagecodes() {
        return instance.languageDetector.getCodes();
    }

    public static String getLanguage(String code) {
        return instance.languageDetector.getLanguage(code);
    }

    public static String guestLanguage(String text) {
        Language best = instance.languageDetector.guestLanguage(text);
        return getLanguage(best.getLang());
    }

    public static String[] predictLanguage(String text) {
        return predictLanguage(text, false);
    }

    public static String[] predictLanguage(String text, boolean all) {
        Language[] langs = instance.languageDetector.predictLanguages(text);
        if (null == langs || langs.length == 0)
            return new String[0];
        long max = 3;
        if (all) {
            max = langs.length;
        }
        return Arrays.stream(langs)
                .limit(max)
                .map(l -> getLanguage(l.getLang()))
                .toArray(String[]::new);
    }

    public static void predictLanguage(String text, PrintStream out) {
        Language[] langs = instance.languageDetector.predictLanguages(text);
        if (null == langs || langs.length == 0)
            return;
        out.println(text);
        for (Language lang : langs) {
            out.println(String.format("\t%s :\t%.4f", getLanguage(lang.getLang()), lang.getConfidence()));
        }
    }

}
