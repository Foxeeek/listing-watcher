package com.listingwatcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.listingwatcher.olx.OlxClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Map;

public class OlxClientTest {
    OlxClient client = new OlxClient(HttpClient.newHttpClient());
    private static final ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(client.buildRequestBody(40,20));

    public OlxClientTest() throws IOException {
    }

    @Test
    @DisplayName("Query content check test")
    void queryContentCheckTest() throws JsonProcessingException {
        System.out.println(node.get("query"));
        Assertions.assertTrue(node.path("query").asText().contains("ListingSearchQuery($searchParameters: [SearchParameter!] = [])"));
    }

    @Test
    @DisplayName("Parameter content check")
    void parameterContentCheckTest(){
        Map<String, String> params = new HashMap<>();
        for (JsonNode parameter : node.at("/variables/searchParameters")){
            params.put(parameter.get("key").asText(),parameter.get("value").asText());
        }
        Assertions.assertEquals("40",params.get("offset"));
        Assertions.assertEquals("20",params.get("limit"));
        Assertions.assertEquals("1151",params.get("category_id"));
        Assertions.assertEquals("created_at:desc",params.get("sort_by"));
        Assertions.assertEquals("free",params.get("filter_enum_price"));

    }
}
