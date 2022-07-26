package org.metal.specs;

import org.metal.Metal;
import org.metal.Pair;

import java.util.ArrayList;
import java.util.List;

public class Spec {
    private String version;
    private List<Metal> metals;
    private List<Pair<String, String>> edges;

    public Spec() {
    }

    public Spec(String version) {
        this.version = version;
        this.metals = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public String getVersion() {
        return version;
    }
    public List<Metal> getMetals() {
        return metals;
    }
    public List<Pair<String, String>> getEdges() {
        return edges;
    }
}
