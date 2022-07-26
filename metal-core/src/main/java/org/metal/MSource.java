package org.metal;

import org.metal.props.IMSourceProps;

public abstract class MSource<R, P extends IMSourceProps> extends Metal <P>{
    public MSource(P props) {
        super(props);
    }

    @Override
    public void forge(ForgeMaster master) {
    }

    public abstract R source();
}
