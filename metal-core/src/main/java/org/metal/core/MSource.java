package org.metal.core;

import java.io.IOException;
import org.metal.core.props.IMSourceProps;
import org.metal.exception.MetalTranslateException;
import org.metal.translator.Translator;
import org.metal.translator.TranslatorContext;

public abstract class MSource<D, S, P extends IMSourceProps> extends Metal<D, S, P> {

  public MSource(String id, String name, P props) {
    super(id, name, props);
  }

  @Override
  public void translate(Translator<D, S> master, TranslatorContext<D, S> context)
      throws MetalTranslateException {
    try {
      master.stageDF(this, this.source(master.platform()), context);
    } catch (IOException e) {
      throw new MetalTranslateException(e);
    }
  }

  public abstract D source(S platform) throws MetalTranslateException;
}
