package org.metal.draft;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.metal.core.MFusion;
import org.metal.core.MMapper;
import org.metal.core.MSink;
import org.metal.core.MSource;
import org.metal.core.Metal;

public class Draft {

  public static class Builder {

    private final Draft inner = new Draft();

    private Builder() {
    }

    public Builder add(Metal metal) throws NullPointerException, NoSuchElementException {
      ((MutableGraph<Metal>) inner.graph).addNode(
          Optional.of(metal).get()
      );
      return this;
    }

    public Builder addEdge(Metal source, Metal target) throws NullPointerException,
        NoSuchElementException,
        IllegalArgumentException {
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
          throw new IllegalArgumentException(
              MSource.class + " can not accept anything from the other metals.");
        } else if (!(target instanceof MFusion) && targetInDegree > 1) {
          throw new IllegalArgumentException("Except for " + MFusion.class
              + ", the other metal can not accept more than 1 input.");
        }
      }

      ((MutableGraph<Metal>) inner.graph).putEdge(source, target);
      return this;
    }

    public WithWaitFor withWait() {
      return new WithWaitFor(this);
    }

    public Draft build() throws IllegalArgumentException {
      inner.graph = ImmutableGraph.copyOf(inner.graph);
      inner.waitFor = ImmutableGraph.copyOf(inner.waitFor);
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

      for (MSink mSink : mSinks) {
        if (inner.graph.inDegree(mSink) == 0) {
          String msg = String.format("MSink{%s} don\'t have any input!", mSink);
          throw new IllegalArgumentException(msg);
        }

        if (inner.graph.outDegree(mSink) != 0) {
          String msg = String.format("MSink{%s} should not have any output!", mSink);
          throw new IllegalArgumentException(msg);
        }
      }

      for (MSource mSource : mSources) {
        if (inner.graph.inDegree(mSource) != 0) {
          String msg = String.format("MSource{%s} should not have any input!", mSource);
          throw new IllegalArgumentException(msg);
        }
      }

      for (MMapper mMapper : mMappers) {
        if (inner.graph.inDegree(mMapper) != 1) {
          String msg = String.format("MMapper{%s} must have only one input!", mMapper);
          throw new IllegalArgumentException(msg);
        }
      }

      for (MFusion mFusion : mFusions) {
        if (inner.graph.inDegree(mFusion) < 2) {
          String msg = String.format("MFusion{%s} must have at least two inputs!", mFusion);
          throw new IllegalArgumentException(msg);
        }
      }

      inner.sources = Collections.unmodifiableSet(mSources);
      inner.mappers = Collections.unmodifiableSet(mMappers);
      inner.fusions = Collections.unmodifiableSet(mFusions);
      inner.sinks = Collections.unmodifiableSet(mSinks);
      return inner;
    }
  }


  public static class WithWaitFor {

    private Builder innerBuilder;

    private WithWaitFor(Builder builder) {
      this.innerBuilder = builder;
    }

    public WithWaitFor waitFor(Metal affectedMetal, MSink mSink) throws IllegalArgumentException {
      Graph<Metal> graph = innerBuilder.inner.graph;
      Graph<MSink> waitFor = innerBuilder.inner.waitFor;

      if (graph.predecessors(mSink).contains(affectedMetal)) {
        String msg = String.format("AffectedMetal %s can\'t be predecessor of MSink %s.",
            affectedMetal, mSink);
        throw new IllegalArgumentException();
      }

      Set<MSink> affectedSinks = graph.successors(affectedMetal).stream().filter((Metal m) -> {
        return m instanceof MSink;
      }).map((Metal m) -> {
        return (MSink) m;
      }).collect(Collectors.toSet());

      if (affectedMetal instanceof MSink) {
        affectedSinks.add((MSink) affectedMetal);
      }

      for (MSink affectedSink : affectedSinks) {
        ((MutableGraph<MSink>) waitFor).putEdge(mSink, affectedSink);
      }

      return this;
    }

    public Draft build() throws IllegalArgumentException {
      return this.innerBuilder.build();
    }
  }

  private Set<MSource> sources;
  private Set<MMapper> mappers;
  private Set<MFusion> fusions;
  private Set<MSink> sinks;
  private Graph<Metal> graph;
  private Graph<MSink> waitFor;

  private String uuid;
  private String prevUuid;

  private Draft() {
    this.graph = GraphBuilder.directed().build();
    this.waitFor = GraphBuilder.directed().build();
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

  public Graph<MSink> getWaitFor() {
    return waitFor;
  }

  public String uuid() {
    return this.uuid;
  }

  public String prevUuid() {
    return this.prevUuid;
  }

  @Override
  public String toString() {

    String graph = StreamSupport.stream(
        Traverser
            .forGraph(this.getGraph())
            .breadthFirst(this.getSources())
            .spliterator(),
        false
    ).collect(Collectors.toList()).toString();

    List<MSink> starters = this.waitFor.nodes().stream().filter((MSink sink) -> {
      return this.waitFor.inDegree(sink) == 0;
    }).collect(Collectors.toList());

    System.out.println(this.waitFor.nodes());

    String waitFor = StreamSupport.stream(
        Traverser.forGraph(this.waitFor)
            .breadthFirst(starters)
            .spliterator(),
        false
    ).collect(Collectors.toList()).toString();

    return "Draft{graph=" + graph + "\nwaitFor=" + waitFor + "}";
  }
}
