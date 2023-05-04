package org.metal.backend;

import org.metal.core.props.IMetalProps;
import org.metal.service.BaseMetalService;

public interface IBackend<D, S, P extends IMetalProps> {

  public void start() throws IllegalArgumentException;

  public void stop();

  public <R extends BaseMetalService<D, S, P>> R service() throws IllegalArgumentException;

  public static interface IBuilder<D, S, P extends IMetalProps> {

    public IBuilder conf(String key, Object value);

    public IBuilder setup(ISetup<S> setup);

    public IBuilder deployOptions(BackendDeployOptions<S> options);

    public IBackend<D, S, P> build();
  }
}
