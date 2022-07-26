package org.metal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class IMSourcePropsTest {

    @Test
    public void testSer() {
        IMSourceProps properties = ImmutableIMSourceProps.builder()
                .id("00")
                .name("s-0")
                .schema("{}")
                .build();
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(properties)
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeser() {
        String json = "{\n" +
                "  \"id\" : \"00\",\n" +
                "  \"name\" : \"s-0\",\n" +
                "  \"schema\" : \"{}\"\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(
                    mapper.readValue(json, IMSourceProps.class)
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    public static class Outter {
        public String z;
        public IMSourceProps properties;
    }

    @Test
    public void testInnerSer() {
        IMSourceProps properties = ImmutableIMSourceProps.builder()
                .id("00")
                .name("s-0")
                .schema("{}")
                .build();
        Outter outter = new Outter();
        outter.z = "zz";
        outter.properties = properties;

        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(
                  mapper.writerWithDefaultPrettyPrinter().writeValueAsString(outter)
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInnerDesr() {
        String json = "{\n" +
                "  \"z\" : \"zz\",\n" +
                "  \"properties\" : {\n" +
                "    \"id\" : \"00\",\n" +
                "    \"name\" : \"s-0\",\n" +
                "    \"schema\" : \"{}\"\n" +
                "  }\n" +
                "}";

        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(
                    mapper.readValue(json, Outter.class)
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
