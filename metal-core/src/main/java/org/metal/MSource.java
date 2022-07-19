package org.metal;

public abstract class MSource <R> extends Metal{
    @Override
    public void forge(ForgeMaster master) {
    }

    public abstract R source();
}
