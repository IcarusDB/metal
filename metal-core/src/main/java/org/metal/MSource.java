package org.metal;

public abstract class MSource <R> extends Metal{
    public MSource() {}
    public MSource(String id, String name) {
        super(id, name);
    }

    @Override
    public void forge(ForgeMaster master) {
    }

    public abstract R source();
}
