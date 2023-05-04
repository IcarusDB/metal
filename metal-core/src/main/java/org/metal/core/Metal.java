package org.metal.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.metal.core.props.IMetalProps;
import org.metal.exception.MetalTranslateException;
import org.metal.translator.Translator;
import org.metal.translator.TranslatorContext;

@JsonTypeInfo(use = Id.CLASS, property = "type", include = As.PROPERTY, visible = false)
public abstract class Metal<D, S, P extends IMetalProps> {

  @JsonProperty("id")
  private String id;

  @JsonProperty("name")
  private String name;

  @JsonProperty
  private P props;

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
    return "Metal{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", props=" + props +
        '}';
  }
}
