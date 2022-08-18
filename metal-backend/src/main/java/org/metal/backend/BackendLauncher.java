package org.metal.backend;

import org.metal.draft.Draft;
import org.metal.draft.DraftMaster;
import org.metal.service.BaseMetalService;
import org.metal.specs.Spec;
import org.metal.specs.SpecFactoryOnJson;

import java.io.IOException;

public class BackendLauncher {
    static String json = "{\n" +
            "  \"version\" : \"1.0\",\n" +
            "  \"metals\" : [ {\n" +
            "    \"type\" : \"org.metal.backend.spark.extension.JsonFileMSource\",\n" +
            "    \"id\" : \"00-00\",\n" +
            "    \"name\" : \"source-00\",\n" +
            "    \"props\" : {\n" +
            "      \"schema\" : \"\",\n" +
            "      \"path\" : \"hdfs://namenode.hdfs.node:9000/test.json\"\n" +
            "    }\n" +
            "  }, {\n" +
            "    \"type\" : \"org.metal.backend.spark.extension.ConsoleMSink\",\n" +
            "    \"id\" : \"01-00\",\n" +
            "    \"name\" : \"sink-00\",\n" +
            "    \"props\" : {\n" +
            "      \"numRows\" : 10\n" +
            "    }\n" +
            "  } ],\n" +
            "  \"edges\" : [ {\n" +
            "    \"left\" : \"00-00\",\n" +
            "    \"right\" : \"01-00\"\n" +
            "  } ],\n" +
            "  \"waitFor\" : [ ]\n" +
            "}";

    public static void main(String[] args) throws IOException {
        IBackend backend = BackendManager.getBackendBuilder().get()
                .build();
        backend.start();
        BaseMetalService service = backend.service();

        Spec spec = new SpecFactoryOnJson().get(json);
        Draft draft = DraftMaster.draft(spec);
        service.analyse(draft);
        service.exec();
        backend.stop();
//        service.analyse();
    }
}
