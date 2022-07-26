package org.metal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.metal.props.IMetalProps;

import java.util.NoSuchElementException;
import java.util.Optional;

@JsonTypeInfo(use = Id.CLASS, property = "type", include = As.PROPERTY, visible = false)
public abstract class Metal <P extends IMetalProps> {

    @JsonProperty
    private P props;

    public Metal(P props) {
        this.props = props;
    }

    public abstract void forge(ForgeMaster master);

    public P props() throws NullPointerException, NoSuchElementException {
        return Optional.of(props).get();
    }

    public String id() throws NullPointerException, NoSuchElementException {
        return Optional.of(props).get().id();
    }

    public String name() throws NullPointerException, NoSuchElementException {
        return Optional.of(props).get().name();
    }
}
