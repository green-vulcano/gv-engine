package it.greenvulcano.gvesb.api.controller;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.greenvulcano.gvesb.api.dto.ExceptionDTO;

public class BaseControllerRest {

    private final static ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
    }

    protected String toJson(Object o) throws JsonProcessingException {

        return OBJECT_MAPPER.writeValueAsString(o);
    }

    protected String toJson(Exception exception) {

        ExceptionDTO dto = new ExceptionDTO(exception);
        return dto.toString();
    }

    protected <T> T parseJson(String json, Class<T> c) throws JsonParseException, JsonMappingException, IOException {

        return OBJECT_MAPPER.readValue(json, c);
    }

}
