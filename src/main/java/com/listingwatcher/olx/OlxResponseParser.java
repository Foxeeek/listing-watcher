package com.listingwatcher.olx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.listingwatcher.model.Listing;
import com.listingwatcher.model.Category;
import com.listingwatcher.model.Location;
import com.listingwatcher.model.Photos;


import java.time.OffsetDateTime;

import java.util.ArrayList;
import java.util.List;

public class OlxResponseParser {
    private static final ObjectMapper mapper = new ObjectMapper();


    private JsonNode requireField(JsonNode parentNode, String fieldName){
        JsonNode field = parentNode.get(fieldName);
        if (field == null || field.isNull()) {
            throw new IllegalArgumentException("Bad response: missing required field " + fieldName);
        }
        return field;
    }

    public List<Listing> parse(String json) throws JsonProcessingException {

        JsonNode responseNode = mapper.readTree(json).at("/data/clientCompatibleListings");
        List<Listing> listings = new ArrayList<>();

        if (!responseNode.path("__typename").asText().equals("ListingSuccess")) {
            throw new IllegalArgumentException("Page not found or bad in endpoint response. __typename is " + responseNode.path("__typename").asText());
        }
        if (!responseNode.path("data").isArray()) {
            throw new IllegalArgumentException("Array with Json data is missing " + responseNode.path("data"));
        }
        for (JsonNode listingNode : responseNode.path("data")) {
            listings.add(toListings(listingNode));
        }

        return listings;
    }


    private Listing toListings(JsonNode node) {
        JsonNode categoryNode = requireField(node, "category");
        JsonNode locationNode = requireField(node, "location");
        JsonNode photosNode = requireField(node, "photos");
        return new Listing(

                requireField(
                        node,
                        "id")
                        .asLong(),

                requireField(
                        node,
                        "title")
                        .asText(),

                node
                        .path("description")
                        .asText(""),

                requireField(
                        node,
                        "url")
                        .asText(),

                OffsetDateTime
                        .parse(
                                requireField(
                                        node,
                                        "created_time")
                                        .asText()),
                OffsetDateTime
                        .parse(
                                requireField(
                                        node,
                                        "last_refresh_time")
                                        .asText()),

                new Category(
                        requireField(categoryNode, "id").asLong(),
                        requireField(categoryNode, "type").asText()),

                new Location(
                        requireField(requireField(locationNode, "city"), "name").asText(),
                        requireField(requireField(locationNode, "region"), "name").asText()),

                new Photos(parsePhoto(photosNode))
        );
    }

    private List<String> parsePhoto(JsonNode photoNode) {
        List<String> photos = new ArrayList<>();
        for (JsonNode photo : photoNode) {
            photos.add(photo.path("link").asText());
        }
        return photos;
    }
}
