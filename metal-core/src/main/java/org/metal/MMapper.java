package org.metal;

import org.metal.props.IMMapperProps;

import java.io.IOException;
import java.util.List;

public abstract class MMapper <D, S, P extends IMMapperProps> extends Metal <D, S, P>{
    public MMapper(String id, String name, P props) {
        super(id, name, props);
    }

    @Override
    public void forge(ForgeMaster<D, S> master) throws IOException {
        D data = master.dependency(this).get(0);
        master.stageDF(this, map(data));
    }

    public abstract D map(D data);
}
