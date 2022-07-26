package org.metal;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.NoSuchElementException;
import java.util.Optional;

public class DraftMaster {
    public static Draft draft(Spec spec) throws NullPointerException, NoSuchElementException {
        spec = Optional.of(spec).get();

        BiMap<String, Metal> id2Metals = HashBiMap.create(spec.getMetals().size());
        Draft.Builder builder = Draft.builder();

        spec.getMetals().stream().forEach(builder::add);
        spec.getMetals().stream().map((metal) -> {
            return Pair.of(metal.getId(), metal);
        }).forEach(pair -> {
            id2Metals.put(pair.getLeft(), pair.getRight());
        });

        spec.getEdges().stream().map(pair -> {
            return Pair.of(id2Metals.get(pair.getLeft()), id2Metals.get(pair.getRight()));
        }).forEach(pair -> {
            builder.addEdge(pair.getLeft(), pair.getRight());
        });

        return builder.build();
    }
}
