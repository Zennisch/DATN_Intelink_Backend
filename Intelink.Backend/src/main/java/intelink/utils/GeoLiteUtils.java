package intelink.utils;

import com.maxmind.geoip2.DatabaseReader;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

public class GeoLiteUtils {

    @Getter
    private static final DatabaseReader cityDatabaseReader;

    static {
        try {
            File database = new File("src/main/resources/geoLite2/GeoLite2-City.mmdb");
            cityDatabaseReader = new DatabaseReader.Builder(database).build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize GeoIP database reader", e);
        }
    }

}
