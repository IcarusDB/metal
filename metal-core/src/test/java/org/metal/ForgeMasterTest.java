package org.metal;

import com.google.common.collect.HashMultimap;
import com.google.common.hash.HashCode;
import org.junit.Test;
import org.metal.props.IMetalProps;
import org.metal.specs.Spec;
import org.metal.specs.SpecFactory;
import org.metal.specs.SpecFactoryOnJson;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class ForgeMasterTest {
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
    public void testForge() throws IOException {
        SpecFactory specFactory = new SpecFactoryOnJson();
        Spec spec = specFactory.get(json);
        Draft draft = DraftMaster.draft(spec);
        ForgeMaster master = new ForgeMaster();
        master.forge(draft);
        HashMap<HashCode, IMProduct> mProducts = master.context().mProducts();
        mProducts.forEach((HashCode hashCode, IMProduct mProduct) -> {
            System.out.println(hashCode.toString());
            mProduct.exec();
        });

        HashMap<HashCode, String> dfs = master.context().dfs();
        dfs.forEach((HashCode hashCode, String df) -> {
            System.out.println(hashCode);
            System.out.println(df);
        });

        HashMultimap<HashCode, Metal<String, Thread, IMetalProps>> hash2metal = master.context().hash2metal();
        hash2metal.forEach((hashcode, metal) -> {
            System.out.println(hashcode);
            System.out.println(metal);
        });

        HashMap<Metal<String, Thread, IMetalProps>, HashCode> metal2hash = master.context().metal2hash();
        metal2hash.forEach((metal, hash) -> {
            System.out.println(metal);
            System.out.println(hash);
        });


    }
}
