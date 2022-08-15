package org.metal.core;

import org.metal.core.exception.MetalForgeException;
import org.metal.core.translator.TranslatorContext;
import org.metal.core.translator.Translator;
import org.metal.core.props.IMFusionProps;

import java.io.IOException;
import java.util.Map;

public abstract class MFusion <D, S, P extends IMFusionProps> extends Metal<D, S, P> {
    public MFusion(String id, String name, P props) {
        super(id, name, props);
    }

    @Override
    public void translate(Translator<D, S> master, TranslatorContext<D, S> context) throws MetalForgeException {
        Map<String, D> datas = master.dependencyWithId(this, context);
        try {
            master.stageDF(this, fusion(master.platform(), datas), context);
        } catch (IOException e) {
            throw new MetalForgeException(e);
        }
    }

    public abstract D fusion(S platform, Map<String, D> datas) throws MetalForgeException;
}
