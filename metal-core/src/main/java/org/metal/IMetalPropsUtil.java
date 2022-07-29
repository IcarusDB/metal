package org.metal;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.metal.props.IMetalProps;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class IMetalPropsUtil {
    public static HashCode sha256(IMetalProps props) throws NullPointerException, IOException {
        Objects.requireNonNull(props);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(props);
        return Hashing.sha256().hashBytes(buffer.toByteArray());
    }

    public static HashCode sha256WithPrev(IMetalProps props, List<HashCode> prevs)  throws NullPointerException, IOException {
        List<HashCode> hashCodes = new ArrayList<>();
        hashCodes.addAll(prevs);
        hashCodes.add(sha256(props));
        return Hashing.combineOrdered(hashCodes);
    }
}
