package org.metal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.graph.Traverser;
import com.google.common.hash.HashCode;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ForgeMaster <D, S> {
    private ForgeContext<D, S> stagingContext;

    public ForgeMaster() {
        this.stagingContext = ImmutableForgeContext.<D, S>builder()
                .dfs(new HashMap<>())
                .hash2metal(HashMultimap.create())
                .metal2hash(new HashMap<>())
                .mProducts(new HashMap<>())
                .draft(Draft.builder().build())
                .build();
    }

    public ForgeMaster(ForgeContext<D, S> context) {
        this.stagingContext = context;
    }

    public D stagingDF(Metal metal, ForgeContext<D, S> context) {
        return context.dfs().get(context.metal2hash().get(metal));
    }

    public void stageDF(Metal metal, D df, ForgeContext<D, S> context) throws IOException {
        HashCode hashCode = IMetalPropsUtil.sha256WithPrev(
                metal.props(),
                context.draft().getGraph().predecessors(metal).stream()
                        .map(context.metal2hash()::get)
                        .sorted(Comparator.comparing(HashCode::toString))
                        .collect(Collectors.toList())
        );
        context.metal2hash().put(metal, hashCode);
        context.hash2metal().put(hashCode, metal);
        context.dfs().put(hashCode, df);
    }

    public void stageIMProduct(Metal metal, IMProduct product, ForgeContext<D, S> context) throws IOException {
        HashCode hashCode = IMetalPropsUtil.sha256WithPrev(
                metal.props(),
                context.draft().getGraph().predecessors(metal).stream()
                        .map(context.metal2hash()::get)
                        .sorted(Comparator.comparing(HashCode::toString))
                        .collect(Collectors.toList())
        );
        context.metal2hash().put(metal, hashCode);
        context.hash2metal().put(hashCode, metal);
        context.mProducts().put(hashCode, product);
    }

    public List<D> dependency(Metal metal, ForgeContext<D, S> context) {
        return context.draft().getGraph().predecessors(metal).stream()
                .map(context.metal2hash()::get)
                .sorted(Comparator.comparing(HashCode::toString))
                .map(context.dfs()::get)
                .collect(Collectors.toList());
    }

    public void forge(Draft draft) throws IOException{
        HashMultimap<HashCode, Metal> hash2metal = HashMultimap.create();
        HashMap<Metal, HashCode> metal2hash = new HashMap<>();

        Iterable<Metal> dependencyTrace = Traverser.forGraph(draft.getGraph())
                .breadthFirst(draft.getSources());

        for (Metal metal : dependencyTrace) {
            HashCode hashCode = IMetalPropsUtil.sha256WithPrev(
                    metal.props(),
                    draft.getGraph().predecessors(metal).stream()
                            .map(metal2hash::get)
                            .sorted(Comparator.comparing(HashCode::toString))
                            .collect(Collectors.toList())
            );
            metal2hash.put(metal, hashCode);
            hash2metal.put(hashCode, metal);
        }

        Set<HashCode> retain = new HashSet<>(stagingContext.dfs().keySet());
        retain.retainAll(hash2metal.keySet());

        HashMap<HashCode, D> dfs = new HashMap<>();
        for (HashCode hashCode: stagingContext.dfs().keySet()) {
            if (retain.contains(hashCode)) {
               dfs.put(hashCode, stagingContext.dfs().get(hashCode));
            }
        }

        HashMap<HashCode, IMProduct> mProducts = new HashMap<>();
        for (HashCode hashCode: stagingContext.mProducts().keySet()) {
            if (retain.contains(hashCode)) {
                mProducts.put(hashCode, stagingContext.mProducts().get(hashCode));
            }
        }

        ForgeContext<D, S> nextContext = (ForgeContext<D, S>) ImmutableForgeContext
                .<D, S>builder()
                .draft(draft)
                .dfs(dfs)
                .hash2metal(hash2metal)
                .metal2hash(metal2hash)
                .mProducts(mProducts)
                .build();
        /***
         * Switch Context
         */
        this.stagingContext = nextContext;

        List<Metal> unStagingDependencyTrace = StreamSupport.stream(dependencyTrace.spliterator(), false)
                .filter(metal -> {
                    return !retain.contains(metal2hash.get(metal));
                })
                .collect(Collectors.toList());

        for (Metal metal : unStagingDependencyTrace) {
            metal.forge(this, nextContext);
        }
    }

    public ForgeContext<D, S> context() {
        return stagingContext;
    }
}
