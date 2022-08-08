package org.metal;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.metal.specs.Spec;

import java.util.NoSuchElementException;
import java.util.Optional;

public class DraftMaster {
    public static Draft draft(Spec spec) throws NullPointerException, NoSuchElementException, IllegalArgumentException {
        spec = Optional.of(spec).get();

        BiMap<String, Metal> id2Metals = HashBiMap.create(spec.getMetals().size());
        Draft.Builder builder = Draft.builder();

        spec.getMetals().stream().forEach(builder::add);
        spec.getMetals().stream().map((metal) -> {
            return Pair.of(metal.id(), metal);
        }).forEach(pair -> {
            id2Metals.put(pair.left(), pair.right());
        });

        if (id2Metals.size() != spec.getMetals().size()) {
            throw new IllegalArgumentException();
        }

        spec.getEdges().stream().map(pair -> {
            return Pair.of(id2Metals.get(pair.left()), id2Metals.get(pair.right()));
        }).forEach(pair -> {
            builder.addEdge(pair.left(), pair.right());
        });

        return builder.build();
    }
}
