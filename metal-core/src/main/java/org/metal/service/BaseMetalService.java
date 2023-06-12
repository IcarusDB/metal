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

package org.metal.service;

import org.metal.core.MSink;
import org.metal.core.Metal;
import org.metal.core.props.IMetalProps;
import org.metal.draft.Draft;
import org.metal.exception.MetalAnalysedException;
import org.metal.exception.MetalExecuteException;
import org.metal.exception.MetalServiceException;
import org.metal.exception.MetalTranslateException;
import org.metal.translator.Translator;
import org.metal.translator.TranslatorContext;

import org.apache.arrow.vector.types.pojo.Schema;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;
import com.google.common.hash.HashCode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BaseMetalService<D, S, P extends IMetalProps> implements IMetalService<D, S, P> {

    private Translator<D, S> translator;

    protected BaseMetalService(Translator<D, S> translator) {
        this.translator = translator;
    }

    public static <D, S, P extends IMetalProps> BaseMetalService<D, S, P> of(
            Translator<D, S> translator) {
        return new BaseMetalService<>(translator);
    }

    @Override
    public D df(String id) throws NoSuchElementException {
        Metal metal = this.context().id2metal().get(id);
        if (metal == null) {
            String msg = String.format("Metal{id=%s} can\'t found in context.", id);
            throw new NoSuchElementException(msg);
        }

        if (metal instanceof MSink) {
            String msg =
                    String.format("Metal{id=%s} is one MSink in context. It didn\'t have DF.", id);
            throw new NoSuchElementException(msg);
        }

        HashCode hashCode = this.context().metal2hash().get(metal);
        if (hashCode == null) {
            String msg = String.format("Metal{%s} can\'t found hashcode in context.", metal);
            throw new NoSuchElementException(msg);
        }

        return this.context().dfs().get(hashCode);
    }

    @Override
    public Metal<D, S, P> metal(String id) throws NoSuchElementException {
        Metal metal = this.context().id2metal().get(id);
        if (metal == null) {
            String msg = String.format("Metal{id=%s} can\'t found in context.", id);
            throw new NoSuchElementException(msg);
        }
        return metal;
    }

    @Override
    public List<String> analysed() {
        Set<HashCode> analysed = this.context().hash2metal().keySet();
        return analysed.stream()
                .flatMap(
                        (code) -> {
                            return this.context().hash2metal().get(code).stream().map(Metal::id);
                        })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> unAnalysed() {
        Set<HashCode> analysed = this.context().hash2metal().keySet();
        return this.context().metal2hash().keySet().stream()
                .map(Metal::id)
                .filter(
                        (id) -> {
                            HashCode code =
                                    this.context()
                                            .metal2hash()
                                            .get(this.context().id2metal().get(id));
                            return !analysed.contains(code);
                        })
                .collect(Collectors.toList());
    }

    @Override
    public void analyse(Draft draft) throws MetalAnalysedException {
        if (this.context().id2metal().size() == this.context().draft().getGraph().nodes().size()) {
            try {
                this.translator.translate(draft);
            } catch (MetalTranslateException e) {
                throw new MetalAnalysedException(e);
            }
        } else {
            /** The Translator will not change context. */
            throw new MetalAnalysedException("Some metals has same id.");
        }
    }

    @Override
    public void exec() throws MetalExecuteException {
        Graph<MSink> wait = GraphBuilder.directed().build();
        for (MSink sink : this.context().draft().getWaitFor().nodes()) {
            ((MutableGraph<MSink>) wait).addNode(sink);
        }
        for (EndpointPair<MSink> edge : this.context().draft().getWaitFor().edges()) {
            ((MutableGraph<MSink>) wait).putEdge(edge);
        }

        for (MSink sink : this.context().draft().getSinks()) {
            ((MutableGraph<MSink>) wait).addNode(sink);
        }

        Set<MSink> starters =
                wait.nodes().stream()
                        .filter(
                                (MSink sink) -> {
                                    return wait.inDegree(sink) == 0;
                                })
                        .collect(Collectors.toSet());

        List<HashCode> execOrder =
                StreamSupport.stream(
                                Traverser.forGraph(wait).breadthFirst(starters).spliterator(),
                                false)
                        .map(
                                (MSink sink) -> {
                                    return this.context().metal2hash().get(sink);
                                })
                        .collect(Collectors.toList());

        LinkedHashSet<HashCode> execOrderDeDup = new LinkedHashSet<>(execOrder);

        for (HashCode code : execOrderDeDup) {
            if (!this.context().mProducts().containsKey(code)) {
                String metals =
                        this.context().hash2metal().get(code).stream()
                                .map(m -> m.id())
                                .collect(Collectors.joining(",", "{", "}"));
                String msg =
                        String.format(
                                "MSink{%s}{hashcode=%s} is not used in any IMProducts.",
                                this.context().hash2metal().get(code), code);
                throw new MetalExecuteException(msg, metals);
            }
        }

        for (HashCode code : execOrderDeDup) {
            try {
                this.context().mProducts().get(code).exec();
            } catch (Throwable t) {
                String metals =
                        this.context().hash2metal().get(code).stream()
                                .map(m -> m.id())
                                .collect(Collectors.joining(",", "{", "}"));
                throw new MetalExecuteException(t.getLocalizedMessage(), t, metals);
            }
        }
    }

    @Override
    public Schema schema(String id) throws MetalServiceException {
        throw new MetalServiceException("This method is not implemented.");
    }

    protected TranslatorContext<D, S> context() {
        return this.translator.context();
    }

    protected Translator<D, S> translator() {
        return this.translator;
    }
}
