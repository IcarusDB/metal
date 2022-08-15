package org.metal.core.draft;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.metal.core.MSink;
import org.metal.core.Metal;
import org.metal.core.Pair;
import org.metal.core.specs.Spec;

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

        Draft.WithWaitFor withWait = builder.withWait();

        for (Pair<String, String> wait : spec.getWaitFor()) {
            Metal metal = id2Metals.get(wait.right());
            if (!(metal instanceof MSink)) {
                String msg = String.format("Metal{%s} is must be MSink.", wait.right());
                throw new IllegalArgumentException(msg);
            }
            MSink mSink = (MSink) metal;
            Metal affectedMetal = id2Metals.get(wait.left());
            withWait.waitFor(affectedMetal, mSink);
        }

        return withWait.build();
    }
}
