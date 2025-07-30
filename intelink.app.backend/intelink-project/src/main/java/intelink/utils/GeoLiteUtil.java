package intelink.utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

@Slf4j
public class GeoLiteUtil {

    @Getter
    private static final DatabaseReader cityDatabaseReader;

    static {
        try {
            File database = new File("src/main/resources/geoLite2/GeoLite2-City.mmdb");
            cityDatabaseReader = new DatabaseReader.Builder(database).build();
        } catch (IOException e) {
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
