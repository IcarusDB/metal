package org.metal.core;

import org.junit.Test;
import org.metal.core.draft.Draft;

public class DraftTest {
    @Test
    public void case0() {
        Mock.MSourceImpl mSource = new Mock.MSourceImpl(
                "00-00",
                "s-00",
                ImmutableMSourcePropsFoo.builder().schema("").build());
        Mock.MMapperImpl mapper = new Mock.MMapperImpl(
                "10-00",
                "m-00",
                ImmutableMMapperPropsFoo.builder().build()
        );

        Mock.MSinkImpl msink = new Mock.MSinkImpl(
                "20-00",
                "sk-00",
                ImmutableMSinkPropsFoo.builder().build()
        );

        Mock.MSinkImpl msink1 = new Mock.MSinkImpl(
                "20-01",
                "sk-01",
                ImmutableMSinkPropsFoo.builder().build()
        );

        Draft draft = Draft.builder()
                .add(mSource).add(mapper).add(msink).add(msink1)
                .addEdge(mSource, mapper)
                .addEdge(mapper, msink)
                .addEdge(mapper, msink1)
                .withWait().waitFor(msink1, msink)
                .build();
        System.out.println(draft);

    }

}
