package com.yidian.push.utils;

/**
 * Created by yidianadmin on 14-10-20.
 */

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

// not tested...
public class JacksonUtil {
    public static final ObjectMapper mapper = new ObjectMapper();
    public static final JsonFactory factory = new JsonFactory();

    public static final TypeReference<List<String>> ListOfStringTypeReference = new TypeReference<List<String>>() {};

    static {
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static String getJsonStr(Object obj) {
        try {
            String jsonStr = mapper.writeValueAsString(obj);
            return jsonStr;
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "{}";
    }

    public static <T> T getMappedObject(Object val, Class<T> type) {
        T ret = null;
        try {
            String jsonStr = mapper.writeValueAsString(val);
            ret = mapper.readValue(jsonStr, type);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static <T> T getMappedObject(JsonNode node, Class<T> type) {
        T ret = null;
        try {
            ret = mapper.readValue(node, type);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static <T> T getMappedObject(JsonNode node, TypeReference<?> valueTypeRef) {
        T ret = null;
        try {
            ret = mapper.readValue(node, valueTypeRef);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static <T> T getMappedObject(String jsonStr, Class<T> type) {
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        T ret = null;
        try {
            ret = mapper.readValue(jsonStr, type);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static JsonNode getJsonObject(String data) throws JsonParseException, JsonMappingException, IOException {
        JsonNode rootNode = mapper.readValue(data, JsonNode.class);
        return rootNode;
    }

    public static ObjectNode getObjectNode() {
        ObjectNode node = mapper.createObjectNode();
        return node;
    }

    public static ArrayNode getArrayNode() {
        ArrayNode node = mapper.createArrayNode();
        return node;
    }

    public static JsonParser createJsonParser(String json) throws JsonParseException, IOException {
        return factory.createJsonParser(json);
    }
}