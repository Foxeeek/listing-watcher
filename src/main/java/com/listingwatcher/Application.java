package com.listingwatcher;

import com.listingwatcher.model.Listing;
import com.listingwatcher.olx.OlxClient;
import com.listingwatcher.olx.OlxResponseParser;
import com.listingwatcher.olx.OlxSource;
import com.listingwatcher.source.Source;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

public class Application {
    private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public static void main(String[] args) throws IOException, InterruptedException {
        OlxClient olxClient = new OlxClient(client);
        OlxResponseParser olxResponseParser = new OlxResponseParser();
        Source source = new OlxSource(olxClient, olxResponseParser);

        List<Listing> listingList = source.fetchLatest();

        System.out.println(listingList.size());

        for (int i = 0; i < Math.min(listingList.size(),3); i++) {
            System.out.println(listingList.get(i).title());
        }


    }

}
