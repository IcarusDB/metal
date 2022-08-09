package org.metal.core;

import org.metal.core.forge.ForgeContext;
import org.metal.core.forge.ForgeMaster;
import org.metal.core.props.IMFusionProps;

import java.io.IOException;
import java.util.List;

public abstract class MFusion <D, S, P extends IMFusionProps> extends Metal<D, S, P> {
    public MFusion(String id, String name, P props) {
        super(id, name, props);
    }

    @Override
    public void forge(ForgeMaster<D, S> master, ForgeContext<D, S> context) throws IOException {
        List<D> datas = master.dependency(this, context);
        master.stageDF(this, fusion(master.platform(), datas), context);
    }

    public abstract D fusion(S platform, List<D> datas);
}
