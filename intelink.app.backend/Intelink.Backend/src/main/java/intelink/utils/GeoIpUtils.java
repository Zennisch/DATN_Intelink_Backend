package intelink.utils;

import com.maxmind.geoip2.DatabaseReader;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

public class GeoIpUtils {

    @Getter
    private static final DatabaseReader databaseReader;

    static {
        try {
            File database = new File("src/main/resources/geo_lite_2/GeoLite2-City.mmdb");
            databaseReader = new DatabaseReader.Builder(database).build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize GeoIP database reader", e);
        }
    }

}
