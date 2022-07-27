package org.metal;

import org.metal.props.IMSourceProps;

public abstract class MSource<R, P extends IMSourceProps> extends Metal <P>{
    public MSource(String id, String name, P props) {
        super(id, name, props);
    }

    @Override
    public void forge(ForgeMaster master) {
    }

    public abstract R source();
}
