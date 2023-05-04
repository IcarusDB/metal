/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metal.core;

import java.io.IOException;
import org.metal.core.props.IMSinkProps;
import org.metal.exception.MetalTranslateException;
import org.metal.translator.Translator;
import org.metal.translator.TranslatorContext;

public abstract class MSink<D, S, P extends IMSinkProps> extends Metal<D, S, P> {

  public MSink(String id, String name, P props) {
    super(id, name, props);
  }

  @Override
  public void translate(Translator<D, S> master, TranslatorContext<D, S> context)
      throws MetalTranslateException {
    D data = master.dependency(this, context).get(0);
    try {
      master.stageIMProduct(this, sink(master.platform(), data), context);
    } catch (IOException e) {
      throw new MetalTranslateException(e);
    } catch (MetalTranslateException e) {
      throw e;
    } catch (Exception e) {
      throw new MetalTranslateException(e);
    }
  }

  public abstract IMExecutor sink(S platform, D data) throws MetalTranslateException;
}
