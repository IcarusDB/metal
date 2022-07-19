package org.metal;

import com.google.common.graph.Graph;

import java.util.Set;

public class Draft {
    private Set<MSource> sources;
    private Set<MMapper> mappers;
    private Set<MFusion> fusions;
    private Set<MSink> sinks;
    private Graph<Metal> graph;

    private String uuid;
    private String prevUuid;

    public Set<MSource> getSources() {
        return sources;
    }

    public Set<MMapper> getMappers() {
        return mappers;
    }

    public Set<MFusion> getFusions() {
        return fusions;
    }

    public Set<MSink> getSinks() {
        return sinks;
    }

    public Graph<Metal> getGraph() {
        return graph;
    }

    public String uuid() {
        return this.uuid;
    }

    public String prevUuid() {
        return this.prevUuid;
    }
}
