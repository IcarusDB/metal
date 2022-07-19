package org.metal;

public abstract class MSink <T> extends Metal{
    @Override
    public void forge(ForgeMaster master) {

    }

    public abstract void sink(T data);
}
