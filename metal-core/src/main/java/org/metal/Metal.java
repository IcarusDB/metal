package org.metal;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.CLASS, property = "type", include = As.PROPERTY, visible = false)
public abstract class Metal {
    private String id;
    private String name;
    public Metal() {}
    public Metal(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public abstract void forge(ForgeMaster master);

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
