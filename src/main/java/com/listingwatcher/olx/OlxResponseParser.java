package com.listingwatcher.olx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.listingwatcher.model.Listing;
import com.listingwatcher.provider.Response;

import java.io.IOException;
import java.util.List;
public class OlxResponseParser {
    final private ObjectMapper mapper = new ObjectMapper();

    public OlxResponseParser() {
        mapper.findAndRegisterModules();
    }
    public List<Listing> read(String json) throws IOException {

        Response response = mapper.readValue(
                json,
                Response.class
        );
        if (response.getData().getClientCompatibleListings().get__typename().equals("ListingError")){
            throw new IllegalArgumentException("Error: invalid response __typename = " + response.getData().getClientCompatibleListings().get__typename());
        }
        return response
                        .getData()
                        .getClientCompatibleListings()
                        .getData();
    }




}
