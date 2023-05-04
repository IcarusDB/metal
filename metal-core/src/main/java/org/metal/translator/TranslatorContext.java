package org.metal.translator;

import com.google.common.collect.HashMultimap;
import com.google.common.hash.HashCode;
import java.util.HashMap;
import org.immutables.value.Value;
import org.metal.core.IMExecutor;
import org.metal.core.Metal;
import org.metal.draft.Draft;

@Value.Immutable
public interface TranslatorContext<D, S> {

  public Draft draft();

  public HashMap<HashCode, D> dfs();

  public HashMap<Metal, HashCode> metal2hash();

  public HashMultimap<HashCode, Metal> hash2metal();

  public HashMap<HashCode, IMExecutor> mProducts();

  public HashMap<String, Metal> id2metal();
}
