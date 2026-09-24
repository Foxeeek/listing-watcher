import com.listingwatcher.olx.OlxResponseParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Application {



    public static void main(String[] args) throws IOException, InterruptedException {
        List<Integer> banList = new ArrayList<>();
        banList.add(1884);
        String file = Files.readString(Path.of("src/test/resources/fixtures/search-page1.json"));
        String file1 = Files.readString(Path.of("src/test/resources/fixtures/search-error-bad-limit.json"));

        OlxResponseParser olxResponseParser = new OlxResponseParser();
        OlxResponseParser olxResponseParser1 = new OlxResponseParser();
        olxResponseParser.read(file);
        olxResponseParser1.read(file1);
    }

}
