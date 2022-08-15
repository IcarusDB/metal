package org.metal.core;

import org.metal.core.exception.MetalForgeException;
import org.metal.core.translator.TranslatorContext;
import org.metal.core.translator.Translator;
import org.metal.core.props.IMMapperProps;

import java.io.IOException;

public abstract class MMapper <D, S, P extends IMMapperProps> extends Metal <D, S, P>{
    public MMapper(String id, String name, P props) {
        super(id, name, props);
    }

    @Override
    public void translate(Translator<D, S> master, TranslatorContext<D, S> context) throws MetalForgeException {
        D data = master.dependency(this, context).get(0);
        try {
            master.stageDF(this, map(master.platform(), data), context);
        } catch (IOException e) {
            throw new MetalForgeException(e);
        }
    }

    public abstract D map(S platform, D data) throws MetalForgeException;
}
