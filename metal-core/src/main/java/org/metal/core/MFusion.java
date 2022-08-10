package org.metal.core;

import org.metal.core.exception.MetalForgeException;
import org.metal.core.forge.ForgeContext;
import org.metal.core.forge.ForgeMaster;
import org.metal.core.props.IMFusionProps;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class MFusion <D, S, P extends IMFusionProps> extends Metal<D, S, P> {
    public MFusion(String id, String name, P props) {
        super(id, name, props);
    }

    @Override
    public void forge(ForgeMaster<D, S> master, ForgeContext<D, S> context) throws MetalForgeException {
        Map<String, D> datas = master.dependencyWithId(this, context);
        try {
            master.stageDF(this, fusion(master.platform(), datas), context);
        } catch (IOException e) {
            throw new MetalForgeException(e);
        }
    }

    public abstract D fusion(S platform, Map<String, D> datas) throws MetalForgeException;
}
