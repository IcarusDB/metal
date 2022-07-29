package org.metal;

import com.google.common.collect.*;
import com.google.common.graph.Traverser;
import com.google.common.hash.HashCode;
import org.junit.Test;
import org.metal.props.IMetalProps;
import org.metal.specs.SpecFactory;
import org.metal.specs.SpecFactoryOnJson;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DraftMasterTest {
    private String json = "{\n" +
            "  \"version\" : \"0.0.1\",\n" +
            "  \"metals\" : [ {\n" +
            "    \"type\" : \"org.metal.Mock$MSourceImpl\",\n" +
            "    \"id\" : \"00-00\",\n" +
            "    \"name\" : \"source-00\",\n" +
            "    \"props\" : {\n" +
            "      \"schema\" : \"{}\"\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"type\" : \"org.metal.Mock$MMapperImpl\",\n" +
            "    \"id\" : \"01-00\",\n" +
            "    \"name\" : \"mapper-00\",\n" +
            "    \"props\" : { }\n" +
            "  }, {\n" +
            "    \"type\" : \"org.metal.Mock$MMapperImpl\",\n" +
            "    \"id\" : \"01-01\",\n" +
            "    \"name\" : \"mapper-01\",\n" +
            "    \"props\" : { }\n" +
            "  }, {\n" +
            "    \"type\" : \"org.metal.Mock$MFusionImpl\",\n" +
            "    \"id\" : \"02-00\",\n" +
            "    \"name\" : \"fusion-00\",\n" +
            "    \"props\" : { }\n" +
            "  }, {\n" +
            "    \"type\" : \"org.metal.Mock$MSinkImpl\",\n" +
            "    \"id\" : \"03-00\",\n" +
            "    \"name\" : \"sink-00\",\n" +
            "    \"props\" : { }\n" +
            "  } ],\n" +
            "  \"edges\" : [ {\n" +
            "    \"left\" : \"00-00\",\n" +
            "    \"right\" : \"01-00\"\n" +
            "  }, {\n" +
            "    \"left\" : \"00-00\",\n" +
            "    \"right\" : \"01-01\"\n" +
            "  }, {\n" +
            "    \"left\" : \"01-00\",\n" +
            "    \"right\" : \"02-00\"\n" +
            "  }, {\n" +
            "    \"left\" : \"01-01\",\n" +
            "    \"right\" : \"02-00\"\n" +
            "  }, {\n" +
            "    \"left\" : \"02-00\",\n" +
            "    \"right\" : \"03-00\"\n" +
            "  } ]\n" +
            "}";
    @Test
    public void draft() {
        SpecFactory factory = new SpecFactoryOnJson();
        try {
            Draft draft = DraftMaster.draft(factory.get(json));
            System.out.println(draft);
            Traverser.forGraph(draft.getGraph()).breadthFirst(draft.getSources()).forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSha256() throws IOException {
        SpecFactory factory = new SpecFactoryOnJson();
        Draft draft = DraftMaster.draft(factory.get(json));
        Multimap<HashCode, Metal> hash2metal = HashMultimap.create();
        Map<Metal, HashCode> metal2hash = new HashMap<>();

        Iterable<Metal> dependTrace = Traverser.forGraph(draft.getGraph())
                .breadthFirst(draft.getSources());
        dependTrace.forEach((Metal metal) -> {
                    List<HashCode> prevs = draft.getGraph().predecessors(metal).stream()
                            .map(metal2hash::get)
                            .sorted(Comparator.comparing(HashCode::toString))
                            .collect(Collectors.toList());
                    try {
                        HashCode hashCode = IMetalPropsUtil.sha256WithPrev(metal.props(), prevs);
                        metal2hash.put(metal, hashCode);
                        hash2metal.put(hashCode, metal);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        dependTrace.forEach((Metal metal) -> {
            System.out.println(metal);
            System.out.println(metal2hash.get(metal));
        });
    }
}
