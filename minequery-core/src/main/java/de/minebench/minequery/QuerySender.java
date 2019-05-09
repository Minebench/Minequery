package de.minebench.minequery;

import java.util.ArrayList;
import java.util.List;

public class QuerySender {
    private final MinequeryPlugin plugin;
    protected List<String> response = new ArrayList<>();
    private String name;

    public QuerySender(MinequeryPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public List<String> getResponse() {
        return response;
    }

    public String getName() {
        return name;
    }
}
