package org.metal.core.forge;

import com.google.common.collect.HashMultimap;
import com.google.common.hash.HashCode;
import org.immutables.value.Value;
import org.metal.core.IMProduct;
import org.metal.core.Metal;
import org.metal.core.draft.Draft;

import java.util.HashMap;

@Value.Immutable
public interface ForgeContext <D, S> {
    public Draft draft();
    public HashMap<HashCode, D> dfs();
    public HashMap<Metal, HashCode> metal2hash();
    public HashMultimap<HashCode, Metal> hash2metal();
    public HashMap<HashCode, IMProduct> mProducts();
    public HashMap<String, Metal> id2metal();
}
