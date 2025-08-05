package intelink.utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;

public class GeoLiteUtilsTests {

    @Test
    void testGeoIpUtils() throws IOException, GeoIp2Exception {
        DatabaseReader databaseReader = GeoLiteUtils.getCityDatabaseReader();
        String ipv6 = "2402:800:6f5f:1c6f:3d45:124:ac23:d797";
        CityResponse cityResponse = databaseReader.city(InetAddress.getByName(ipv6));
        String countryIsoCode = cityResponse.getCountry().getIsoCode();
        String countryName = cityResponse.getCountry().getName();
        String cityName = cityResponse.getCity().getName();

        System.out.println("Country ISO Code: " + countryIsoCode);
        System.out.println("Country Name: " + countryName);
        System.out.println("City Name: " + cityName);
    }

}
