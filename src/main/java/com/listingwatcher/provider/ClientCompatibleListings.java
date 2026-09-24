package com.listingwatcher.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.listingwatcher.model.Listing;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientCompatibleListings {
    private String __typename;
    private List<Listing> data;


    public String get__typename() {
        return __typename;
    }

    public void set__typename(String __typename) {
        this.__typename = __typename;
    }

    public List<Listing> getData() {
        return data;
    }

    public void setData(List<Listing> data) {
        this.data = data;
    }

    // getters/setters
}