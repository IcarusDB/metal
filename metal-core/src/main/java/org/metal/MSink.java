package org.metal;

import org.metal.props.IMSinkProps;

public abstract class MSink <T, P extends IMSinkProps> extends Metal <P>{
    public MSink(String id, String name, P props) {
        super(id, name, props);
    }

    @Override
    public void forge(ForgeMaster master) {

    }

    public abstract void sink(T data);
}
