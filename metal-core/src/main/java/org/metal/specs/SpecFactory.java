package org.metal.specs;

import org.metal.specs.Spec;

import java.io.IOException;

public interface SpecFactory {
    public Spec get(byte[] data) throws IOException;
    public Spec get(String data) throws IOException;
}
