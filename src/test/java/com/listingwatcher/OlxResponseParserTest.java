package com.listingwatcher;


import com.listingwatcher.model.Listing;
import com.listingwatcher.olx.OlxResponseParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class OlxResponseParserTest {
    private final OlxResponseParser parser = new OlxResponseParser();

    @Test
    @DisplayName("Test when we have normal file ")
    void searchPage1Test() throws IOException {
        String json = Files.readString(Path.of("src/test/resources/fixtures/search-page1.json"));
        List<Listing> listingList = parser.parse(json);

        Assertions.assertEquals(26, listingList.size());
        Assertions.assertEquals(1099032519, listingList.getFirst().id());
    }

    @Test
    @DisplayName("Throw test")
    void searchErrorBadLimitTest() throws IOException {
        String json = Files.readString(Path.of("src/test/resources/fixtures/search-error-bad-limit.json"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> parser.parse(json));

    }

    @Test
    @DisplayName("Check exception without id in json file")
    void throwExceptionMiniJsonTest() throws IOException {
        String json = Files.readString(Path.of("src/test/resources/fixtures/listing-without-id.json"));

        IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, () -> parser.parse(json));
        Assertions.assertEquals("Bad response: missing required field id", illegalArgumentException.getMessage());
    }

    @Test
    @DisplayName("Test parsing city")
    void parseCityTest() throws IOException {
        String json = Files.readString(Path.of("src/test/resources/fixtures/search-page1.json"));
        List<Listing> listingList = parser.parse(json);

        Assertions.assertEquals("Imielin", listingList.getFirst().location().city());
    }
}
