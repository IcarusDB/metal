package org.metal.core;

import java.io.IOException;
import java.util.Map;
import org.metal.core.props.IMFusionProps;
import org.metal.exception.MetalTranslateException;
import org.metal.translator.Translator;
import org.metal.translator.TranslatorContext;

public abstract class MFusion<D, S, P extends IMFusionProps> extends Metal<D, S, P> {

  public MFusion(String id, String name, P props) {
    super(id, name, props);
  }

  @Override
  public void translate(Translator<D, S> master, TranslatorContext<D, S> context)
      throws MetalTranslateException {
    Map<String, D> datas = master.dependencyWithId(this, context);
    try {
      master.stageDF(this, fusion(master.platform(), datas), context);
    } catch (IOException e) {
      throw new MetalTranslateException(e);
    }
  }

  public abstract D fusion(S platform, Map<String, D> datas) throws MetalTranslateException;
}
