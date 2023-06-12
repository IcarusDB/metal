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

import org.metal.core.props.IMFusionProps;
import org.metal.exception.MetalTranslateException;
import org.metal.translator.Translator;
import org.metal.translator.TranslatorContext;

import java.io.IOException;
import java.util.Map;

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
