package com.listingwatcher.olx;

import com.listingwatcher.model.Listing;
import com.listingwatcher.source.Source;

import java.io.IOException;
import java.util.List;

public class OlxSource implements Source {
    private final OlxResponseParser olxResponseParser;
    private final OlxClient olxClient;

    private static final int OFFSET = 0;
    private static final int LIMIT = 40;

    public OlxSource(OlxClient olxClient, OlxResponseParser olxResponseParser) {
        this.olxClient = olxClient;
        this.olxResponseParser = olxResponseParser;
    }

    @Override
    public List<Listing> fetchLatest() throws IOException, InterruptedException {
        return olxResponseParser.parse(olxClient.search(OFFSET,LIMIT));
    }
}
