package org.metal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.metal.props.IMFusionProps;

import java.util.List;

public abstract class MFusion <T, R, P extends IMFusionProps> extends Metal <P>{
    public MFusion(P props) {
        super(props);
    }

    @Override
    public void forge(ForgeMaster master) {

    }

    public abstract R fusion(List<T> datas);
}
