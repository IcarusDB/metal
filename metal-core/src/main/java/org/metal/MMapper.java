package org.metal;

public abstract class MMapper <T, R> extends Metal{
    public MMapper() {}
    public MMapper(String id, String name) {
        super(id, name);
    }

    @Override
    public void forge(ForgeMaster master) {

    }

    public abstract R map(T data);
}
