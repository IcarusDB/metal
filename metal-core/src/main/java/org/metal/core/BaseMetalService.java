package org.metal.core;

import com.google.common.hash.HashCode;
import org.metal.core.draft.Draft;
import org.metal.core.exception.MetalAnalysedException;
import org.metal.core.exception.MetalExecuteException;
import org.metal.core.exception.MetalForgeException;
import org.metal.core.forge.ForgeContext;
import org.metal.core.forge.ForgeMaster;
import org.metal.core.props.IMetalProps;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BaseMetalService<D, S, P extends IMetalProps> implements IMetalService <D, S, P> {
    private ForgeMaster<D, S> forgeMaster;
    protected BaseMetalService(ForgeMaster<D, S> forgeMaster) {
        this.forgeMaster = forgeMaster;
    }

    public static <D, S, P extends IMetalProps> BaseMetalService<D, S, P> of(ForgeMaster<D, S> forgeMaster) {
        return new BaseMetalService<>(forgeMaster);
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
                this.forgeMaster.forge(draft);
            } catch (MetalForgeException e) {
                throw new MetalAnalysedException(e);
            } catch (IllegalStateException e) {
                throw new IllegalStateException(e);
            }
        } else {
            /**
             * The ForgeMaster will not change context.
             */
            throw new IllegalStateException("Some metals has same id.");
        }
    }

    @Override
    public void exec() throws MetalExecuteException {
        for (Map.Entry<HashCode, IMProduct> kv : this.context().mProducts().entrySet()) {
            try {
                kv.getValue().exec();
            } catch (Throwable t) {
                throw new MetalExecuteException(t);
            }
        }
    }

    private ForgeContext<D, S> context() {
        return this.forgeMaster.context();
    }
}
