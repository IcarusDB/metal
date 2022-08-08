package org.metal;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.hash.HashCode;
import org.immutables.value.Value;

import java.util.HashMap;
import java.util.Map;

@Value.Immutable
public interface ForgeContext <D, S> {
    public Draft draft();
    public HashMap<HashCode, D> dfs();
    public HashMap<Metal, HashCode> metal2hash();
    public HashMultimap<HashCode, Metal> hash2metal();
    public HashMap<HashCode, IMProduct> mProducts();
}
