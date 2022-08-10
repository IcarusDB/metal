package org.metal.core;

import org.metal.core.draft.Draft;
import org.metal.core.exception.MetalAnalysedException;
import org.metal.core.exception.MetalExecuteException;
import org.metal.core.props.IMetalProps;

import java.util.List;

public interface IMetalService <D, S, P extends IMetalProps>{
    public D df(String id);
    public Metal<D, S, P> metal(String id);
    public List<String> analysed();
    public List<String> unAnalysed();
    public void analyse(Draft draft) throws MetalAnalysedException, IllegalStateException;
    public void exec() throws MetalExecuteException;
}
