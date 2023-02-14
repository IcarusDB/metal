package org.metal.specs;

import com.fasterxml.jackson.databind.json.JsonMapper;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import java.io.IOException;
import org.metal.exception.MetalSpecParseException;

public class SpecFactoryOnJson implements SpecFactory{
    private JsonMapper mapper = new JsonMapper();

    @Override
    public Spec get(byte[] data) throws MetalSpecParseException{
        try {
            mapper.registerModule(new GuavaModule());
            return mapper.readValue(data, Spec.class);
        } catch (IOException e) {
            throw new MetalSpecParseException(e);
        }

    }

    @Override
    public Spec get(String data) throws MetalSpecParseException{
        try {
            mapper.registerModule(new GuavaModule());
            return mapper.readValue(data, Spec.class);
        } catch (IOException e) {
            throw new MetalSpecParseException(e);
        }
    }
}
