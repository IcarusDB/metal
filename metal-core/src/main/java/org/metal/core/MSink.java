package org.metal.core;

import org.metal.core.exception.MetalExecuteException;
import org.metal.core.exception.MetalForgeException;
import org.metal.core.forge.ForgeContext;
import org.metal.core.forge.ForgeMaster;
import org.metal.core.props.IMSinkProps;

import java.io.IOException;

public abstract class MSink <D, S, P extends IMSinkProps> extends Metal <D, S, P>{
    public MSink(String id, String name, P props) {
        super(id, name, props);
    }

    @Override
    public void forge(ForgeMaster<D, S> master, ForgeContext<D, S> context) throws MetalForgeException {
        D data = master.dependency(this, context).get(0);
        try {
            master.stageIMProduct(this, new IMProduct() {
                @Override
                public void exec() throws MetalExecuteException {
                    sink(master.platform(), data);
                }
            }, context);
        } catch (IOException e) {
            throw new MetalForgeException(e);
        }
    }

    public abstract void sink(S platform, D data) throws MetalExecuteException;
}
