package org.metal;

import org.metal.props.IMSinkProps;

public abstract class MSink <T, P extends IMSinkProps> extends Metal <P>{

    public MSink(P props) {
        super(props);
    }

    @Override
    public void forge(ForgeMaster master) {

    }

    public abstract void sink(T data);
}
