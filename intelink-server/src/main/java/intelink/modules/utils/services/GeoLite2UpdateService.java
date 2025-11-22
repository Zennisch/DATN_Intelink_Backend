package intelink.modules.utils.services;

import com.maxmind.geoip2.DatabaseReader;
import intelink.utils.GeoLiteUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class GeoLite2UpdateService {

    private static final String DOWNLOAD_URL = "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-City&license_key=%s&suffix=tar.gz";
    @Value("${app.geolite2.license-key}")
    private String licenseKey;
    @Value("${app.geolite2.database.path}")
    private String databasePath;
    @Value("${app.geolite2.update.enabled}")
    private boolean updateEnabled;

    @Scheduled(initialDelay = 0, fixedRateString = "${app.geolite2.update.schedule.fixed-rate}")
    public void updateGeoLite2Database() {
        if (!updateEnabled) {
            log.info("GeoLite2 updater is disabled. Skipping download and reload.");
            Path dbPath = Paths.get(databasePath);
            log.info("Attempting to load existing GeoLite2 database from: {}", dbPath.toAbsolutePath());
            if (Files.exists(dbPath)) {
                try {
                    byte[] mmdbData = Files.readAllBytes(dbPath);
                    reloadDatabaseReader(mmdbData);
                    log.info("Loaded existing GeoLite2 database from: {}", dbPath.toAbsolutePath());
                } catch (Exception e) {
                    log.error("Failed to load existing GeoLite2 database: {}", e.getMessage(), e);
                }
            } else {
                log.warn("GeoLite2 database file not found at: {}. If updates are disabled, no database will be available.", dbPath.toAbsolutePath());
            }
            return;
        }

        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            log.warn("GeoLite2 license key is not configured. Skipping database update.");
            return;
        }

        log.info("Starting GeoLite2 database update...");

        try {
            byte[] tarGzData = downloadDatabase();
            byte[] mmdbData = extractMmdbFromTarGz(tarGzData);

            saveDatabaseFile(mmdbData);
            reloadDatabaseReader(mmdbData);

            log.info("GeoLite2 database updated successfully");
        } catch (Exception e) {
            log.error("Failed to update GeoLite2 database: {}", e.getMessage(), e);
        }
    }

    private byte[] downloadDatabase() throws IOException {
        String url = String.format(DOWNLOAD_URL, licenseKey);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download database. HTTP response code: " + responseCode);
        }

        try (InputStream is = connection.getInputStream(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            log.info("Database downloaded successfully");
            return baos.toByteArray();
        }
    }

    private byte[] extractMmdbFromTarGz(byte[] tarGzData) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(tarGzData);
             GzipCompressorInputStream gzis = new GzipCompressorInputStream(bais);
             TarArchiveInputStream tais = new TarArchiveInputStream(gzis)) {

            TarArchiveEntry entry;
            while ((entry = tais.getNextEntry()) != null) {
                if (entry.getName().endsWith("GeoLite2-City.mmdb")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = tais.read(buffer)) != -1) {
                        baos.write(buffer, 0, bytesRead);
                    }
                    log.info("Database extracted successfully");
                    return baos.toByteArray();
                }
            }
            throw new IOException("GeoLite2-City.mmdb not found in archive");
        }
    }

    private void saveDatabaseFile(byte[] mmdbData) throws IOException {
        Path resourcePath = Paths.get("resources", databasePath);
        Files.createDirectories(resourcePath.getParent());
        Files.write(resourcePath, mmdbData);
        log.info("Database saved to: {}", resourcePath.toAbsolutePath());
    }

    private void reloadDatabaseReader(byte[] mmdbData) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(mmdbData)) {
            DatabaseReader newReader = new DatabaseReader.Builder(bais).build();
            GeoLiteUtil.setCityDatabaseReader(newReader);
            log.info("DatabaseReader reloaded successfully");
        }
    }
}