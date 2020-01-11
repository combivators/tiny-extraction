package net.tiny.nlp.open;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Extraction {

    private Map<String, List<String>> tokens = new HashMap<>();

    public Extraction append(String type, String word) {
        List<String> values = tokens.get(type);
        if(null == values) {
            values = new ArrayList<>();
            values.add(word);
            tokens.put(type, values);
        } else if (!values.contains(word)) {
            values.add(word);
        }
        return this;
    }

    public String findFirst(String type) {
        String[] values = find(type);
        if (values.length > 0) {
            return values[0];
        }
        return null;
    }

    public String[] find(String type) {
        List<String> values = tokens.get(type);
        if(null == values) {
            return new String[0];
        } else {
            return values.toArray(new String[values.size()]);
        }
    }


    public String[] types() {
        return tokens.keySet()
                .toArray(new String[tokens.size()]);
    }

    public void output(PrintStream out) {

    }
}
