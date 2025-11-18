package de.muenchen.jpa.criteria;

import jakarta.persistence.criteria.Selection;

import java.util.HashMap;
import java.util.Map;

public class AliasContext {
    private Map<Selection<?>, String> aliases = new HashMap<>();

    private int state = 0;

    public String getAlias(Selection<?> path) {
        return aliases.computeIfAbsent(path, (i) -> getString(state++));
    }

    public void reset() {
        this.state = 0;
        this.aliases.clear();
    }

    private static String getString(int idx) {
        var sb = new StringBuilder();
        var next = idx;
        do {
            char n = (char) ('a'+(next%26));
            sb.append(n);
            next /= 26;
        } while (next > 0);
        return sb.toString();
    }
}
