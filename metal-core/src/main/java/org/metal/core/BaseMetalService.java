package org.metal.core;

import com.google.common.hash.HashCode;
import org.metal.core.draft.Draft;
import org.metal.core.exception.UnAnalysedException;
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
        return List.<String>copyOf(this.context().id2metal().keySet());
    }

    @Override
    public List<String> unAnalysed() {
        Set<String> ids = this.context().id2metal().keySet();
        return this.context().draft().getGraph().nodes()
                .stream()
                .map(Metal::id)
                .filter(id -> !ids.contains(id))
                .collect(Collectors.toList());
    }

    @Override
    public void analyse(Draft draft) throws UnAnalysedException {
        if (this.context().id2metal().size() ==
                this.context().draft().getGraph().nodes().size()) {
            try {
                this.forgeMaster.forge(draft);
            } catch (IOException e) {
                throw new UnAnalysedException();
            }
        } else {
            throw new UnAnalysedException();
        }
    }

    @Override
    public void exec() {
        for (Map.Entry<HashCode, IMProduct> kv : this.context().mProducts().entrySet()) {
            kv.getValue().exec();
        }
    }

    private ForgeContext<D, S> context() {
        return this.forgeMaster.context();
    }
}
