#!/bin/bash

update_geolite2() {
    echo "Updating GeoLite2 database..."
    
    if [ -z "$MAXMIND_LICENSE_KEY" ]; then
        echo "Warning: MAXMIND_LICENSE_KEY not set. Skipping GeoLite2 update."
        return 1
    fi
    
    cd /app/src/main/resources/geoLite2/
    
    curl -L "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-City&license_key=${MAXMIND_LICENSE_KEY}&suffix=tar.gz" -o GeoLite2-City.tar.gz
    
    if [ $? -eq 0 ]; then
        tar -xzf GeoLite2-City.tar.gz --wildcards "*/GeoLite2-City.mmdb" --strip-components=1
        
        if [ -f "GeoLite2-City.mmdb" ]; then
            echo "GeoLite2 database updated successfully."
            rm -f GeoLite2-City.tar.gz
        else
            echo "Error: Failed to extract GeoLite2 database."
            return 1
        fi
    else
        echo "Error: Failed to download GeoLite2 database."
        return 1
    fi
}

if [ "$GEOLITE2_UPDATE_ENABLED" = "true" ]; then
    update_geolite2
fi

exec java $JAVA_OPTS -jar app.jar
