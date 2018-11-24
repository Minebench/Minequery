package de.minebench.minequery;

import java.util.ArrayList;
import java.util.List;

public class QuerySender {
    private final MinequeryPlugin plugin;
    protected List<String> response = new ArrayList<>();

    public QuerySender(MinequeryPlugin plugin) {
        this.plugin = plugin;
    }

    public List<String> getResponse() {
        return response;
    }
}
