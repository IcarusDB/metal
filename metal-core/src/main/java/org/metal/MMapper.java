package org.metal;

public abstract class MMapper <T, R> extends Metal{
    @Override
    public void forge(ForgeMaster master) {

    }

    public abstract R map(T data);
}
