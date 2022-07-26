package org.metal;

import com.google.common.graph.*;

import java.util.*;

public class Draft {

    public static class Builder {
        private Draft inner = new Draft();

        private Builder() {}

        public Builder add(Metal metal) throws NullPointerException, NoSuchElementException {
            metal = Optional.of(metal).get();
            ((MutableGraph<Metal>)inner.graph).addNode(metal);
            return this;
        }

        public Builder addEdge(Metal source, Metal target) throws NullPointerException,
                NoSuchElementException,
                IllegalArgumentException
                {
            source = Optional.of(source).get();
            target = Optional.of(target).get();

            if (source instanceof MSink) {
                throw new IllegalArgumentException(MSink.class + " can not be as edge start.");
            }

            if (target instanceof MSource) {
                throw new IllegalArgumentException(MSource.class + " can not be as edge end.");
            }

            int sourceOutDegree = inner.graph.outDegree(source);
            int targetInDegree = inner.graph.inDegree(target);

            if (source instanceof MSink && sourceOutDegree > 0) {
                throw new IllegalArgumentException(MSink.class + " can not output anything to the others.");
            }

            if (targetInDegree > 0) {
                if (target instanceof MSource) {
                    throw new IllegalArgumentException(MSource.class + " can not accept anything from the other metals.");
                } else if (!(target instanceof MFusion) && targetInDegree > 1) {
                    throw new IllegalArgumentException("Except for "+ MFusion.class + ", the other metal can not accept more than 1 input.");
                }
            }

            ((MutableGraph<Metal>)inner.graph).putEdge(source, target);
            return this;
        }

        public Draft build() {
            inner.graph = ImmutableGraph.copyOf(inner.graph);
            Set<MSource> mSources = new HashSet<>();
            Set<MMapper> mMappers = new HashSet<>();
            Set<MFusion> mFusions = new HashSet<>();
            Set<MSink> mSinks = new HashSet<>();

            inner.graph.nodes().forEach((metal -> {
                if (metal instanceof MSource) {
                    mSources.add((MSource) metal);
                } else if (metal instanceof MMapper) {
                    mMappers.add((MMapper) metal);
                } else if (metal instanceof MFusion) {
                    mFusions.add((MFusion) metal);
                } else if (metal instanceof MSink) {
                    mSinks.add((MSink) metal);
                }
            }));

            inner.sources = Collections.unmodifiableSet(mSources);
            inner.mappers = Collections.unmodifiableSet(mMappers);
            inner.fusions = Collections.unmodifiableSet(mFusions);
            inner.sinks = Collections.unmodifiableSet(mSinks);
            return inner;
        }
    }

    private Set<MSource> sources;
    private Set<MMapper> mappers;
    private Set<MFusion> fusions;
    private Set<MSink> sinks;
    private Graph<Metal> graph;

    private String uuid;
    private String prevUuid;

    private Draft() {
        this.graph = GraphBuilder.directed().build();
    }

    public static Builder builder() {
        return new Builder();
    }

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
