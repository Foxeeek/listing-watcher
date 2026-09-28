package com.listingwatcher.olx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Collectors;

public class OlxClient {
    private static final String FILE_NAME = "/listing-search.graphql";
    private final ObjectMapper mapper;
    private final HttpClient client;
    private final String query;

    private static final String POST_URL = "https://www.olx.pl/apigateway/graphql";
    private static final int DURATION_TIMEOUT = 10;

    public OlxClient(HttpClient client) throws IOException {
        this.client = client;
        this.query = readFileFromResource();
        this.mapper = new ObjectMapper();
    }


    void addParameter(ObjectNode parameter, String key, String value) {
        parameter.put("key", key);
        parameter.put("value", value);
    }

    public String buildRequestBody(int offset, int limit) throws JsonProcessingException {
        ObjectNode root = mapper.createObjectNode();

        root.put("query", query);
        ObjectNode variablesNode = root.putObject("variables");
        ArrayNode searchParameterNode = variablesNode.putArray("searchParameters");

        addParameter(searchParameterNode.addObject(), "offset", Integer.toString(offset));
        addParameter(searchParameterNode.addObject(), "limit", Integer.toString(limit));
        addParameter(searchParameterNode.addObject(), "category_id", "1151");
        addParameter(searchParameterNode.addObject(), "sort_by", "created_at:desc");
        addParameter(searchParameterNode.addObject(), "filter_enum_price", "free");

        return mapper.writeValueAsString(root);


    }

    private String readFileFromResource() throws IOException {
        try (InputStream inputStream = OlxClient.class.getResourceAsStream(FILE_NAME)) {
            if (inputStream == null) {
                throw new IOException("Error reading file: content is missing " + FILE_NAME);
            }
            return new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.joining("\n"));

        }
    }

    public String search(int offset, int limit) throws IOException, InterruptedException {
        HttpRequest pageRequest = postRequest(buildRequestBody(offset, limit));
        HttpResponse<String> response = send(pageRequest);

        String body = response.body();

        if (response.statusCode() != 200) {
            throw new IOException("Response from page is " + response.statusCode() + " Body : " + body.substring(0,Math.min(body.length(),200)));
        }

        return body;
    }

    HttpRequest postRequest(String jsonBody){
        return HttpRequest.newBuilder()
                .uri(URI.create(POST_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(DURATION_TIMEOUT))
                .build();
    }

    HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
