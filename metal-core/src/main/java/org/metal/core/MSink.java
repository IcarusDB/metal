package org.metal.core;

import org.metal.core.forge.ForgeContext;
import org.metal.core.forge.ForgeMaster;
import org.metal.core.props.IMSinkProps;

import java.io.IOException;

public abstract class MSink <D, S, P extends IMSinkProps> extends Metal <D, S, P>{
    public MSink(String id, String name, P props) {
        super(id, name, props);
    }

    @Override
    public void forge(ForgeMaster<D, S> master, ForgeContext<D, S> context) throws IOException {
        D data = master.dependency(this, context).get(0);
        master.stageIMProduct(this, new IMProduct() {
            @Override
            public void exec() throws RuntimeException {
                sink(master.platform(), data);
            }
        }, context);
    }

    public abstract void sink(S platform, D data);
}
