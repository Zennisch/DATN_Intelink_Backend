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
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Slf4j
public class GeoLite2UpdateService {

    private static final String DOWNLOAD_URL = "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-City&license_key=%s&suffix=tar.gz";
    private static final String MMDB_FILENAME = "GeoLite2-City.mmdb";
    private static final int BUFFER_SIZE = 16384;
    private static final int CONNECT_TIMEOUT_MS = 30000;
    private static final int READ_TIMEOUT_MS = 60000;

    @Value("${app.geolite2.license-key}")
    private String licenseKey;

    @Value("${app.geolite2.database.path}")
    private String databasePath;

    @Value("${app.geolite2.update.enabled}")
    private boolean updateEnabled;

    private final ReentrantReadWriteLock databaseLock = new ReentrantReadWriteLock();
    private volatile boolean isUpdating = false;

    @Scheduled(initialDelay = 0, fixedRateString = "${app.geolite2.update.schedule.fixed-rate}")
    public void updateGeoLite2Database() {
        if (isUpdating) {
            log.warn("[GeoLite2UpdateService.updateGeoLite2Database] Database update already in progress. Skipping this run.");
            return;
        }

        if (!updateEnabled) {
            loadExistingDatabase();
            return;
        }

        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            log.warn("[GeoLite2UpdateService.updateGeoLite2Database] GeoLite2 license key is not configured. Skipping database update.");
            return;
        }

        isUpdating = true;
        long startTime = System.currentTimeMillis();
        log.info("[GeoLite2UpdateService.updateGeoLite2Database] Starting GeoLite2 database update...");

        try {
            byte[] tarGzData = downloadDatabase();
            byte[] mmdbData = extractMmdbFromTarGz(tarGzData);

            saveDatabaseFile(mmdbData);
            reloadDatabaseReader(mmdbData);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[GeoLite2UpdateService.updateGeoLite2Database] GeoLite2 database updated successfully in {}ms", duration);
        } catch (IOException e) {
            log.error("[GeoLite2UpdateService.updateGeoLite2Database] Failed to update GeoLite2 database: {}", e.getMessage(), e);
        } finally {
            isUpdating = false;
        }
    }

    private void loadExistingDatabase() {
        log.info("[GeoLite2UpdateService.loadExistingDatabase] GeoLite2 updater is disabled. Attempting to load existing database.");
        Path dbPath = Paths.get(databasePath);

        if (!Files.exists(dbPath)) {
            log.warn("[GeoLite2UpdateService.loadExistingDatabase] GeoLite2 database file not found at: {}. No database will be available.",
                    dbPath.toAbsolutePath());
            return;
        }

        try {
            byte[] mmdbData = Files.readAllBytes(dbPath);
            reloadDatabaseReader(mmdbData);
            log.info("[GeoLite2UpdateService.loadExistingDatabase] Loaded existing GeoLite2 database from: {}", dbPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("[GeoLite2UpdateService.loadExistingDatabase] Failed to load existing GeoLite2 database: {}", e.getMessage(), e);
        }
    }

    private byte[] downloadDatabase() throws IOException {
        log.debug("[GeoLite2UpdateService.downloadDatabase] Connecting to download URL...");
        String url = String.format(DOWNLOAD_URL, licenseKey);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("User-Agent", "GeoLite2UpdateService/1.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Failed to download database. HTTP response code: " + responseCode);
            }

            long contentLength = connection.getContentLengthLong();
            log.debug("[GeoLite2UpdateService.downloadDatabase] Downloading {} bytes...", contentLength);

            try (InputStream is = connection.getInputStream();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream(
                         contentLength > 0 ? (int) Math.min(contentLength, Integer.MAX_VALUE) : 8192)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }

                log.info("[GeoLite2UpdateService.downloadDatabase] Database downloaded successfully. Total bytes: {}", totalBytes);
                return baos.toByteArray();
            }
        } finally {
            connection.disconnect();
        }
    }

    private byte[] extractMmdbFromTarGz(byte[] tarGzData) throws IOException {
        log.debug("[GeoLite2UpdateService.extractMmdbFromTarGz] Extracting MMDB from tar.gz archive...");

        try (ByteArrayInputStream bais = new ByteArrayInputStream(tarGzData);
             GzipCompressorInputStream gzis = new GzipCompressorInputStream(bais);
             TarArchiveInputStream tais = new TarArchiveInputStream(gzis)) {

            TarArchiveEntry entry;
            while ((entry = tais.getNextEntry()) != null) {
                if (entry.getName().endsWith(MMDB_FILENAME)) {
                    log.debug("[GeoLite2UpdateService.extractMmdbFromTarGz] Found {} in archive", MMDB_FILENAME);

                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream((int) entry.getSize())) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int bytesRead;

                        while ((bytesRead = tais.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }

                        log.info("[GeoLite2UpdateService.extractMmdbFromTarGz] Database extracted successfully. Size: {} bytes", baos.size());
                        return baos.toByteArray();
                    }
                }
            }

            throw new IOException(MMDB_FILENAME + " not found in archive");
        }
    }

    private void saveDatabaseFile(byte[] mmdbData) throws IOException {
        log.debug("[GeoLite2UpdateService.saveDatabaseFile] Saving database file...");
        Path resourcePath = Paths.get(databasePath);

        try {
            Files.createDirectories(resourcePath.getParent());
            Files.write(resourcePath, mmdbData);
            log.info("[GeoLite2UpdateService.saveDatabaseFile] Database saved to: {}", resourcePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("[GeoLite2UpdateService.saveDatabaseFile] Failed to save database file: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void reloadDatabaseReader(byte[] mmdbData) throws IOException {
        log.debug("[GeoLite2UpdateService.reloadDatabaseReader] Reloading DatabaseReader...");
        databaseLock.writeLock().lock();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(mmdbData)) {
            DatabaseReader newReader = new DatabaseReader.Builder(bais).build();
            GeoLiteUtil.setCityDatabaseReader(newReader);
            log.info("[GeoLite2UpdateService.reloadDatabaseReader] DatabaseReader reloaded successfully");
        } catch (IOException e) {
            log.error("[GeoLite2UpdateService.reloadDatabaseReader] Failed to reload DatabaseReader: {}", e.getMessage(), e);
            throw e;
        } finally {
            databaseLock.writeLock().unlock();
        }
    }
}