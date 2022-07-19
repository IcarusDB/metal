package org.metal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Spec {
    private String version;
    private List<Metal> metals;
    private List<Map.Entry<String, String>> edges;

    public Spec(String version) {
        this.version = version;
        this.metals = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Metal> getMetals() {
        return metals;
    }

    public void setMetals(List<Metal> metals) {
        this.metals = metals;
    }

    public List<Map.Entry<String, String>> getEdges() {
        return edges;
    }

    public void setEdges(List<Map.Entry<String, String>> edges) {
        this.edges = edges;
    }
}
