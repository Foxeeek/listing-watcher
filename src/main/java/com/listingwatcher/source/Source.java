package com.listingwatcher.source;

import com.listingwatcher.model.Listing;

import java.io.IOException;
import java.util.List;

public interface Source {
    List<Listing> fetchLatest() throws IOException, InterruptedException;
}
