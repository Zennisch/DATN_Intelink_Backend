package intelink.utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

@Component
@Slf4j
public class GeoLiteUtil {

    @Getter
    private static DatabaseReader cityDatabaseReader;

    @Value("${app.geolite2.database.path}")
    private String databasePath;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource(databasePath);

            if (resource.exists()) {
                InputStream inputStream = resource.getInputStream();
                cityDatabaseReader = new DatabaseReader.Builder(inputStream).build();
                log.info("GeoLite2 database loaded successfully from classpath");
            } else {
                log.warn("GeoLite2 database not found in classpath, creating empty reader");
            }
        } catch (IOException e) {
            log.error("GeoLiteUtil - Failed to initialize GeoIP database reader: {}", e.getMessage());
            throw new RuntimeException("GeoLiteUtil - Failed to initialize GeoIP database reader", e);
        }
    }

    public static String getCountryFromIp(String ip) {
        try {
            CityResponse response = cityDatabaseReader.city(InetAddress.getByName(ip));
            String countryIsoCode = response.getCountry().getIsoCode();
            String countryName = response.getCountry().getName();
            if (countryIsoCode != null && countryName != null) {
                return countryIsoCode + " / " + countryName;
            } else if (countryIsoCode != null) {
                return countryIsoCode;
            } else return countryName;
        } catch (Exception e) {
            log.error("GeoLiteUtil.getCountryFromIp - Failed to get country for IP {}: {}", ip, e.getMessage());
            return null;
        }
    }

    public static String getCityFromIp(String ip) {
        try {
            CityResponse response = cityDatabaseReader.city(InetAddress.getByName(ip));
            return response.getCity().getName();
        } catch (Exception e) {
            log.error("GeoLiteUtil.getCityFromIp - Failed to get city for IP {}: {}", ip, e.getMessage());
            return null;
        }
    }

}
