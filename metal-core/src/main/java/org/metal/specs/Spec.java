package org.metal.specs;

import org.metal.core.Metal;
import org.metal.core.Pair;

import java.util.ArrayList;
import java.util.List;

public class Spec {
    public final static String VERSION = "1.0";
    private String version;
    private List<Metal> metals;
    private List<Pair<String, String>> edges;

    /**
     * Left : affected metal id
     * Right : metal id which need to be waited.
     */
    private List<Pair<String, String>> waitFor;

    public Spec() {
        this.waitFor = new ArrayList<>();
    }

    public Spec(String version) {
        this.version = version;
        this.metals = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.waitFor = new ArrayList<>();
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

    public List<Pair<String, String>> getWaitFor() {
        return waitFor;
    }
}
