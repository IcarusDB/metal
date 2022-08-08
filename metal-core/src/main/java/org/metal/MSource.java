package org.metal;

import org.metal.props.IMSourceProps;

import java.io.IOException;

public abstract class MSource<D, S, P extends IMSourceProps> extends Metal <D, S, P>{
    public MSource(String id, String name, P props) {
        super(id, name, props);
    }

    @Override
    public void forge(ForgeMaster<D, S> master, ForgeContext<D, S> context) throws IOException {
        master.stageDF(this, this.source(), context);
    }

    public abstract D source();
}
