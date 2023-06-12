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

import org.metal.core.props.IMetalProps;
import org.metal.exception.MetalTranslateException;
import org.metal.translator.Translator;
import org.metal.translator.TranslatorContext;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.util.NoSuchElementException;
import java.util.Optional;

@JsonTypeInfo(use = Id.CLASS, property = "type", include = As.PROPERTY, visible = false)
public abstract class Metal<D, S, P extends IMetalProps> {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty private P props;

    public Metal(String id, String name, P props) {
        this.id = id;
        this.name = name;
        this.props = props;
    }

    public abstract void translate(Translator<D, S> master, TranslatorContext<D, S> context)
            throws MetalTranslateException;

    public P props() throws NullPointerException, NoSuchElementException {
        return Optional.of(props).get();
    }

    public String id() throws NullPointerException, NoSuchElementException {
        return Optional.of(id).get();
    }

    public String name() throws NullPointerException, NoSuchElementException {
        return Optional.of(name).get();
    }

    @Override
    public String toString() {
        return "Metal{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", props=" + props + '}';
    }
}
