package com.listingwatcher;

import com.listingwatcher.model.Listing;
import com.listingwatcher.olx.OlxResponseParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ListingWatcherTests {
    OlxResponseParser olxResponseParser = new OlxResponseParser();
    @Test
    @DisplayName("OlxResponseParserThrowsTest")
    void OlxResponseParserThrowsTest() throws IOException {

        String file = Files.readString(Path.of("src/test/resources/fixtures/search-error-bad-limit.json"));
        Assertions.assertThrows(IllegalArgumentException.class, ()->olxResponseParser.read(file));
    }
    @Test
    @DisplayName("OlxResponseParserTest")
    void OlxResponseParserTest() throws IOException {
        String file = Files.readString(Path.of("src/test/resources/fixtures/search-page1.json"));
        List<Listing> listingList = olxResponseParser.read(file);
        Assertions.assertEquals(26, listingList.size());
        Assertions.assertEquals(1099032519, listingList.get(0).id());
    }
}
