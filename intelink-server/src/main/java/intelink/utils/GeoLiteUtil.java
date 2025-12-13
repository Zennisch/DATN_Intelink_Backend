package intelink.utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
@Slf4j
public class GeoLiteUtil {

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    @Getter
    private static DatabaseReader cityDatabaseReader;

    public static void setCityDatabaseReader(DatabaseReader newReader) throws IOException {
        lock.writeLock().lock();
        try {
            DatabaseReader oldReader = cityDatabaseReader;
            cityDatabaseReader = newReader;

            if (oldReader != null) {
                oldReader.close();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static String getCountryNameFromIp(String ip) {
        lock.readLock().lock();
        try {
            if (cityDatabaseReader == null) {
                return null;
            }
            CityResponse response = cityDatabaseReader.city(InetAddress.getByName(ip));
            return response.getCountry().getName();
        } catch (Exception e) {
            log.error("GeoLiteUtil.getCountryNameFromIp - Failed to get country for IP {}: {}", ip, e.getMessage());
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static String getCountryIsoFromIp(String ip) {
        lock.readLock().lock();
        try {
            if (cityDatabaseReader == null) {
                return null;
            }
            CityResponse response = cityDatabaseReader.city(InetAddress.getByName(ip));
            return response.getCountry().getIsoCode();
        } catch (Exception e) {
            log.error("GeoLiteUtil.getCountryIsoFromIp - Failed to get country for IP {}: {}", ip, e.getMessage());
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static String getCityFromIp(String ip) {
        lock.readLock().lock();
        try {
            if (cityDatabaseReader == null) {
                return null;
            }
            CityResponse response = cityDatabaseReader.city(InetAddress.getByName(ip));
            return response.getCity().getName();
        } catch (Exception e) {
            log.error("GeoLiteUtil.getCityFromIp - Failed to get city for IP {}: {}", ip, e.getMessage());
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

}
