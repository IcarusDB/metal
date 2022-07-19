package org.metal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.Test;

import java.util.List;
import java.util.Map;

class MSourceImpl extends MSource<String> {
    @Override
    public String source() {
        return "I am source.";
    }
}

class MMapperImpl extends MMapper<String, String> {
    @Override
    public String map(String data) {
        return "I am mapper";
    }
}

public class SpecFactoryOnJsonTest {

    @Test
    public void get() {
        SpecFactory factory = new SpecFactoryOnJson();
        MSourceImpl mSource = new MSourceImpl();
        MMapperImpl mMapper = new MMapperImpl();

        JsonMapper mapper = new JsonMapper();
        mSource.setId("00-01");
        mSource.setName("source-01");
        mMapper.setId("01-01");
        mMapper.setName("mapper-02");

        Spec spec = new Spec("1.0");
        spec.setMetals(List.of(mSource, mMapper));
        spec.setEdges(List.of(Map.entry(mSource.getId(), mMapper.getId())));

        try {
            System.out.println(
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec)
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }
}
