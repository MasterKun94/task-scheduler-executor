package com.oceanum.exec;


import com.fasterxml.jackson.databind.ObjectMapper;

public class Test {

    public static void main(String[] args) throws Exception {
        Exception e = new Exception("this is a test");
        ObjectMapper mapper = new ObjectMapper();
        String str = mapper.writeValueAsString(e);
        System.out.println(str);
        mapper.readValue(str, Throwable.class).printStackTrace();
    }
}
