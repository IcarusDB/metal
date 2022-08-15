package org.metal.core;

import com.google.common.graph.*;
import com.google.common.hash.HashCode;
import org.metal.core.draft.Draft;
import org.metal.core.exception.MetalAnalysedException;
import org.metal.core.exception.MetalExecuteException;
import org.metal.core.exception.MetalForgeException;
import org.metal.core.translator.TranslatorContext;
import org.metal.core.translator.Translator;
import org.metal.core.props.IMetalProps;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BaseMetalService<D, S, P extends IMetalProps> implements IMetalService <D, S, P> {
    private Translator<D, S> translator;
    protected BaseMetalService(Translator<D, S> translator) {
        this.translator = translator;
    }

    public static <D, S, P extends IMetalProps> BaseMetalService<D, S, P> of(Translator<D, S> translator) {
        return new BaseMetalService<>(translator);
    }

    @Override
    public D df(String id) {
        Metal metal = this.context().id2metal().get(id);
        HashCode hashCode = this.context().metal2hash().get(metal);
        return this.context().dfs().get(hashCode);
    }

    @Override
    public Metal<D, S, P> metal(String id) {
        return this.context().id2metal().get(id);
    }

    @Override
    public List<String> analysed() {
        Set<HashCode> analysed = this.context().dfs().keySet();
        return analysed.stream().flatMap((code) -> {
            return this.context().hash2metal().get(code).stream().map(Metal::id);
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> unAnalysed() {
        Set<HashCode> analysed = this.context().dfs().keySet();
        return this.context().metal2hash().keySet()
                .stream()
                .map(Metal::id)
                .filter((id) -> {
                    HashCode code = this.context().metal2hash().get(
                            this.context().id2metal().get(id)
                    );
                    return !analysed.contains(code);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void analyse(Draft draft) throws MetalAnalysedException, IllegalStateException {
        if (this.context().id2metal().size() ==
                this.context().draft().getGraph().nodes().size()) {
            try {
                this.translator.translate(draft);
            } catch (MetalForgeException e) {
                throw new MetalAnalysedException(e);
            } catch (IllegalStateException e) {
                throw new IllegalStateException(e);
            }
        } else {
            /**
             * The Translator will not change context.
             */
            throw new IllegalStateException("Some metals has same id.");
        }
    }

    @Override
    public void exec() throws MetalExecuteException {
        Graph<MSink> wait = GraphBuilder.directed().build();
        for(MSink sink: this.context().draft().getWaitFor().nodes()) {
            ((MutableGraph<MSink>)wait).addNode(sink);
        }
        for(EndpointPair<MSink> edge: this.context().draft().getWaitFor().edges()) {
            ((MutableGraph<MSink>)wait).putEdge(edge);
        }

        for(MSink sink: this.context().draft().getSinks()) {
            ((MutableGraph<MSink>)wait).addNode(sink);
        }

        Set<MSink> starters = wait.nodes().stream().filter((MSink sink)->{
            return wait.inDegree(sink) == 0;
        }).collect(Collectors.toSet());

        List<HashCode> execOrder = StreamSupport.stream(
                Traverser.forGraph(wait)
                        .breadthFirst(starters).spliterator(),
                false
        ).map((MSink sink) -> {
           return this.context().metal2hash().get(sink);
        }).collect(Collectors.toList());

        LinkedHashSet<HashCode> execOrderDeDup = new LinkedHashSet<>(execOrder);

        for (HashCode code : execOrderDeDup) {
            if (!this.context().mProducts().containsKey(code)) {
                String msg = String.format("MSink{%s}{hashcode=%s} is not used in any IMProducts.",
                        this.context().hash2metal().get(code),
                        code);
                throw new MetalExecuteException(msg);
            }
        }

        for (HashCode code : execOrderDeDup) {
            try {
                this.context().mProducts().get(code).exec();
            } catch (Throwable t) {
                throw new MetalExecuteException(t);
            }
        }
    }

    private TranslatorContext<D, S> context() {
        return this.translator.context();
    }
}
