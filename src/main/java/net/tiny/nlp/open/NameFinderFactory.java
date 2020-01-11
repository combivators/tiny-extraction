package net.tiny.nlp.open;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import opennlp.tools.namefind.NameFinderME;

/**
 * Encapsulates OpenNLP's NameFinder by providing a mechanism to load all of the
 * name finder models files found in a single directory into memory and
 * instantiating an array of NameFinderME objects.
 *
 */
public class NameFinderFactory {

    private static final Logger LOGGER = Logger.getLogger(NameFinderFactory.class.getName());

    private final Map<String, NameFinderME> finders;

    public NameFinderFactory() {
        this(new Properties());
    }

    public NameFinderFactory(Properties param) {
        this.finders = new HashMap<>();
        final String path = OpenNLP.getModelPath(param);
        final String language = OpenNLP.getModelLanguage(param);
        final List<URL> resources = findNameFinderModels(path, language);
        for (URL url : resources) {
            NameFinderME finder = OpenNLP.loadNameFinders(url);
            if (null != finder) {
                String modelName = OpenNLP.guestModelName(url);
                finders.put(modelName, finder);
            }
        }
    }

    public NameFinderFactory(String... resources) {
        this.finders = new HashMap<>();
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (String res : resources) {
            URL url = loader.getResource(res);
            if (null != url) {
                append(url);
            } else {
                LOGGER.warning(String.format("Configuration Error: No models in '%s'", res));
            }
        }
    }

    public NameFinderFactory(Map<String, NameFinderME> finders) {
        this.finders = finders;
    }

    protected List<URL> findNameFinderModels(String path, String language) {
        final String modelPrefix = language + "-ner";

        File[] models = new File(path).listFiles(new FilenameFilter() {
            public boolean accept(File file, String name) {
                if (name.startsWith(modelPrefix)) {
                    return true;
                }
                return false;
            }
        });

        if (models == null || models.length < 1) {
            return Collections.emptyList();
        }
        List<URL> res = new ArrayList<>();
        for (File f : models) {
            try {
                res.add(f.toURI().toURL());
            } catch (MalformedURLException e) {}
        }
        return res;
    }

    protected void append(URL url) {
        String modelName = OpenNLP.guestModelName(url);
        if (!finders.containsKey(modelName)) {
            NameFinderME finder = OpenNLP.loadNameFinders(url);
            if (null != finder) {
                finders.put(modelName, finder);
                LOGGER.info(String.format("An OpenNLP '%s' modle was loaded.", modelName));
            }
        }
    }

    public NameFinderFactory merge(NameFinderFactory factory) {
        final Map<String, NameFinderME> copy = new HashMap<>(this.finders);
        copy.putAll(factory.finders);
        return new NameFinderFactory(copy);
    }

    public NameFinderFactory concat(NameFinderFactory factory) {
        finders.putAll(factory.finders);
        return this;
    }

    /**
     * Obtain a reference to the array of NameFinderME's loaded by the engine.
     *
     * @param language
     * @param type
     * @return
     */
    public NameFinderME getNameFinders(String language, String type) {
        String modelName  = OpenNLP.getModelName(language, type);
        NameFinderME finder = finders.get(modelName);
        if (null == finder) {
            // Try to get default model
            modelName  = OpenNLP.getModelName(language, null);
            finder = finders.get(modelName);
        }
        return finder;
    }

    public String[] getAllModelNames() {
        return finders.keySet().toArray(new String[finders.size()]);

    }

    public String[] getModelNames(String language) {
        return finders.keySet()
                .stream()
                .filter(k -> k.startsWith(language))
                .toArray(size -> new String[size]);
    }

    public boolean isEmpty() {
        return finders.isEmpty();
    }

    public void clear() {
        finders.clear();
    }

    protected void finalize() throws Throwable {
        clear();
    }
}
