package org.metal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;
import com.google.common.hash.HashCode;
import org.metal.props.IMetalProps;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ForgeMaster <D, S> {
    private Draft stagingDraft;
    private Map<HashCode, D> stagingDF;
    private Map<Metal, HashCode> stagingMetal2hash;
    private List<IMProduct> stagingMProducts;

    public void updateStaging(Draft draft) {

    }

    public D stagingDF(Metal metal) {
        return stagingDF.get(stagingMetal2hash.get(metal));
    }

    public void stageDF(Metal metal, D df) throws IOException {
        HashCode hashCode = IMetalPropsUtil.sha256WithPrev(
                metal.props(),
                stagingDraft.getGraph().predecessors(metal).stream()
                        .map(stagingMetal2hash::get)
                        .sorted(Comparator.comparing(HashCode::toString))
                        .collect(Collectors.toList())
        );
        stagingMetal2hash.put(metal, hashCode);
        stagingDF.put(stagingMetal2hash.get(metal), df);
    }

    public void stageIMProduct(IMProduct product) {
        this.stagingMProducts.add(product);
    }

    public List<D> dependency(Metal metal) {
        return stagingDraft.getGraph().predecessors(metal).stream()
                .map(stagingMetal2hash::get)
                .sorted(Comparator.comparing(HashCode::toString))
                .map(stagingDF::get)
                .collect(Collectors.toList());
    }

    public List<IMProduct> forge(Draft draft) {
        Multimap<HashCode, Metal> hash2metal = HashMultimap.create();
        Map<Metal, HashCode> metal2hash = new HashMap<>();

        Iterable<Metal> dependencyTrace = Traverser.forGraph(draft.getGraph())
                .breadthFirst(draft.getSources());
        dependencyTrace.forEach((Metal metal) -> {
            try {
                HashCode hashCode = IMetalPropsUtil.sha256WithPrev(
                        metal.props(),
                        draft.getGraph().predecessors(metal).stream()
                                .map(metal2hash::get)
                                .sorted(Comparator.comparing(HashCode::toString))
                                .collect(Collectors.toList())
                );
                metal2hash.put(metal, hashCode);
                hash2metal.put(hashCode, metal);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Set<HashCode> intersection = new HashSet<>(stagingDF.keySet());
        intersection.retainAll(hash2metal.keySet());
        Map<HashCode, D> df = new HashMap<>();
        intersection.forEach(hashCode -> {
            df.put(hashCode, stagingDF.get(hashCode));
        });

        Set<HashCode> unstaging = new HashSet<>(hash2metal.keySet());
        unstaging.removeAll(intersection);
        List<Metal> unstagingDependencyTrace = StreamSupport.stream(dependencyTrace.spliterator(), false)
                .filter(metal -> {
                    return !intersection.contains(metal2hash.get(metal));
                }).collect(Collectors.toList());

        unstagingDependencyTrace.forEach(metal -> {
            try {
                metal.forge(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return null;
    }
}
