package org.metal.core;

import org.metal.core.forge.ForgeContext;
import org.metal.core.forge.ForgeMaster;
import org.metal.core.props.IMMapperProps;

import java.io.IOException;

public abstract class MMapper <D, S, P extends IMMapperProps> extends Metal <D, S, P>{
    public MMapper(String id, String name, P props) {
        super(id, name, props);
    }

    @Override
    public void forge(ForgeMaster<D, S> master, ForgeContext<D, S> context) throws IOException {
        D data = master.dependency(this, context).get(0);
        master.stageDF(this, map(master.platform(), data), context);
    }

    public abstract D map(S platform, D data);
}
