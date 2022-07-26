package org.metal;

public abstract class MSink <T> extends Metal{
    public MSink() {}
    public MSink(String id, String name) {
        super(id, name);
    }

    @Override
    public void forge(ForgeMaster master) {

    }

    public abstract void sink(T data);
}
