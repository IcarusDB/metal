package org.metal;

import java.util.List;

public abstract class MFusion <T, R> extends Metal{
    public MFusion() {}
    public MFusion(String id, String name) {
        super(id, name);
    }

    @Override
    public void forge(ForgeMaster master) {

    }

    public abstract R fusion(List<T> datas);
}
