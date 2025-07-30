package intelink.utils;

import org.junit.jupiter.api.Test;

public class GeoLiteUtilTest {

    @Test
    public void testGetCountryFromIP() {
        String ipAddress = "68.149.154.94";
        String country = GeoLiteUtil.getCountryFromIp(ipAddress);
        System.out.println("Country for IP " + ipAddress + ": " + country);
    }

    @Test
    public void testGetCityFromIP() {
        String ipAddress = "68.149.154.94";
        String city = GeoLiteUtil.getCityFromIp(ipAddress);
        System.out.println("City for IP " + ipAddress + ": " + city);
    }

}
