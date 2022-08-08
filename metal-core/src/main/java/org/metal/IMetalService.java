package org.metal;

import org.metal.props.IMetalProps;

import java.util.List;

public interface IMetalService <D, S, P extends IMetalProps>{
    public D df(String id);
    public Metal<D, S, P> metal(String id);
    public List<String> analysed();
    public List<String> unAnalysed();
    public void analyse(Draft draft);
    public void exec();
}
