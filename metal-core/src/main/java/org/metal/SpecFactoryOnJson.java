package org.metal;

import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;

public class SpecFactoryOnJson implements SpecFactory{
    @Override
    public Spec get(byte[] data) throws IOException{
        JsonMapper mapper = new JsonMapper();
        Spec spec = mapper.readValue(data, Spec.class);
        return spec;
    }

    @Override
    public Spec get(String data) throws IOException {
        JsonMapper mapper = new JsonMapper();
        Spec spec = mapper.readValue(data, Spec.class);
        return spec;
    }
}
