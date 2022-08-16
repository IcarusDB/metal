package org.metal.specs;

import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;

public class SpecFactoryOnJson implements SpecFactory{
    private JsonMapper mapper = new JsonMapper();

    @Override
    public Spec get(byte[] data) throws IOException{
        return mapper.readValue(data, Spec.class);
    }

    @Override
    public Spec get(String data) throws IOException {
        return mapper.readValue(data, Spec.class);
    }
}
