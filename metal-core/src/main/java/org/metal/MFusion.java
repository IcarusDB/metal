package org.metal;

import java.util.List;

public abstract class MFusion <T, R> extends Metal{
    @Override
    public void forge(ForgeMaster master) {

    }

    public abstract R fusion(List<T> datas);
}
