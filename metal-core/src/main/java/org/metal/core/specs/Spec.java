package org.metal.core.specs;

import org.metal.core.Metal;
import org.metal.core.Pair;

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
