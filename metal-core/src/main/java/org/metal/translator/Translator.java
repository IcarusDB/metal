/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metal.translator;

import org.metal.core.IMExecutor;
import org.metal.core.Metal;
import org.metal.core.props.IMetalPropsUtil;
import org.metal.draft.Draft;
import org.metal.exception.MetalTranslateException;

import com.google.common.collect.HashMultimap;
import com.google.common.graph.Traverser;
import com.google.common.hash.HashCode;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Translator<D, S> {

    private volatile TranslatorContext<D, S> stagingContext;
    private S platform;

    public Translator(S platform) {
        this.stagingContext =
                ImmutableTranslatorContext.<D, S>builder()
                        .dfs(new HashMap<>())
                        .hash2metal(HashMultimap.create())
                        .metal2hash(new HashMap<>())
                        .mProducts(new HashMap<>())
                        .id2metal(new HashMap<>())
                        .draft(Draft.builder().build())
                        .build();
        this.platform = platform;
    }

    public Translator(S platform, TranslatorContext<D, S> context) {
        this.stagingContext = context;
        this.platform = platform;
    }

    public D stagingDF(Metal metal, TranslatorContext<D, S> context) {
        return context.dfs().get(context.metal2hash().get(metal));
    }

    public void stageDF(Metal metal, D df, TranslatorContext<D, S> context) throws IOException {
        HashCode hashCode =
                IMetalPropsUtil.sha256WithPrev(
                        metal.props(),
                        context.draft().getGraph().predecessors(metal).stream()
                                .map(context.metal2hash()::get)
                                .sorted(Comparator.comparing(HashCode::toString))
                                .collect(Collectors.toList()));
        context.dfs().put(hashCode, df);
    }

    public void stageIMProduct(Metal metal, IMExecutor product, TranslatorContext<D, S> context)
            throws IOException {
        HashCode hashCode =
                IMetalPropsUtil.sha256WithPrev(
                        metal.props(),
                        context.draft().getGraph().predecessors(metal).stream()
                                .map(context.metal2hash()::get)
                                .sorted(Comparator.comparing(HashCode::toString))
                                .collect(Collectors.toList()));
        context.metal2hash().put(metal, hashCode);
        context.hash2metal().put(hashCode, metal);
        context.mProducts().put(hashCode, product);
    }

    public List<D> dependency(Metal metal, TranslatorContext<D, S> context) {
        return context.draft().getGraph().predecessors(metal).stream()
                .map(context.metal2hash()::get)
                .sorted(Comparator.comparing(HashCode::toString))
                .map(context.dfs()::get)
                .collect(Collectors.toList());
    }

    public Map<String, D> dependencyWithId(Metal metal, TranslatorContext<D, S> context) {
        Set<Metal> dependency = context.draft().getGraph().predecessors(metal);
        Map<String, D> ret = new HashMap<>();
        for (Metal dep : dependency) {
            HashCode code = context.metal2hash().get(dep);
            D df = context.dfs().get(code);
            ret.put(dep.id(), df);
        }
        return Collections.unmodifiableMap(ret);
    }

    public void translate(Draft draft) throws MetalTranslateException {
        HashMultimap<HashCode, Metal> hash2metal = HashMultimap.create();
        HashMap<Metal, HashCode> metal2hash = new HashMap<>();

        Iterable<Metal> dependencyTrace =
                Traverser.forGraph(draft.getGraph()).breadthFirst(draft.getSources());

        for (Metal metal : dependencyTrace) {
            try {
                HashCode hashCode =
                        IMetalPropsUtil.sha256WithPrev(
                                metal.props(),
                                draft.getGraph().predecessors(metal).stream()
                                        .map(metal2hash::get)
                                        .sorted(Comparator.comparing(HashCode::toString))
                                        .collect(Collectors.toList()));
                metal2hash.put(metal, hashCode);
                hash2metal.put(hashCode, metal);
            } catch (IOException e) {
                /** Illegal State happened, the ForgeMaster Context will not change. */
                throw new MetalTranslateException(
                        "Illegal State happened, the Translator Context will not change.",
                        e,
                        metal.id());
            }
        }

        Set<HashCode> retain = new HashSet<>(stagingContext.dfs().keySet());
        retain.retainAll(hash2metal.keySet());

        HashMap<HashCode, D> dfs = new HashMap<>();
        for (HashCode hashCode : stagingContext.dfs().keySet()) {
            if (retain.contains(hashCode)) {
                dfs.put(hashCode, stagingContext.dfs().get(hashCode));
            }
        }

        HashMap<HashCode, IMExecutor> mProducts = new HashMap<>();
        for (HashCode hashCode : stagingContext.mProducts().keySet()) {
            if (retain.contains(hashCode)) {
                mProducts.put(hashCode, stagingContext.mProducts().get(hashCode));
            }
        }

        HashMap<String, Metal> id2metal = new HashMap<>();
        for (Metal metal : metal2hash.keySet()) {
            id2metal.put(metal.id(), metal);
        }

        TranslatorContext<D, S> nextContext =
                (TranslatorContext<D, S>)
                        ImmutableTranslatorContext.<D, S>builder()
                                .draft(draft)
                                .dfs(dfs)
                                .hash2metal(hash2metal)
                                .metal2hash(metal2hash)
                                .mProducts(mProducts)
                                .id2metal(id2metal)
                                .build();
        /***
         * Switch Context
         */
        this.stagingContext = nextContext;

        List<Metal> unStagingDependencyTrace =
                StreamSupport.stream(dependencyTrace.spliterator(), false)
                        .filter(
                                metal -> {
                                    return !retain.contains(metal2hash.get(metal));
                                })
                        .collect(Collectors.toList());

        for (Metal metal : unStagingDependencyTrace) {
            try {
                metal.translate(this, nextContext);
            } catch (MetalTranslateException e) {
                throw new MetalTranslateException(e.getLocalizedMessage(), e, metal.id());
            }
        }
    }

    public TranslatorContext<D, S> context() {
        return stagingContext;
    }

    public S platform() {
        return this.platform;
    }
}
