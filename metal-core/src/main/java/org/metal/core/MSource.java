package org.metal.core;

import org.metal.core.exception.MetalForgeException;
import org.metal.core.forge.ForgeContext;
import org.metal.core.forge.ForgeMaster;
import org.metal.core.props.IMSourceProps;

import java.io.IOException;

public abstract class MSource<D, S, P extends IMSourceProps> extends Metal <D, S, P>{
    public MSource(String id, String name, P props) {
        super(id, name, props);
    }

    @Override
    public void forge(ForgeMaster<D, S> master, ForgeContext<D, S> context) throws MetalForgeException {
        try {
            master.stageDF(this, this.source(master.platform()), context);
        } catch (IOException e) {
            throw new MetalForgeException(e);
        }
    }

    public abstract D source(S platform) throws MetalForgeException;
}
