package org.metal;

import org.metal.props.IMMapperProps;

public abstract class MMapper <T, R, P extends IMMapperProps> extends Metal <P>{

    public MMapper(P props) {
        super(props);
    }

    @Override
    public void forge(ForgeMaster master) {

    }

    public abstract R map(T data);
}
