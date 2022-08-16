package org.metal.service;

import org.apache.arrow.vector.types.pojo.Schema;
import org.metal.core.Metal;
import org.metal.draft.Draft;
import org.metal.exception.MetalAnalysedException;
import org.metal.exception.MetalExecuteException;
import org.metal.core.props.IMetalProps;
import org.metal.exception.MetalServiceException;

import java.util.List;
import java.util.NoSuchElementException;

public interface IMetalService <D, S, P extends IMetalProps>{
    public D df(String id) throws NoSuchElementException;
    public Metal<D, S, P> metal(String id) throws NoSuchElementException;
    public List<String> analysed();
    public List<String> unAnalysed();
    public void analyse(Draft draft) throws MetalAnalysedException, IllegalStateException;
    public void exec() throws MetalExecuteException;
    public Schema schema(String id) throws MetalServiceException;
}
