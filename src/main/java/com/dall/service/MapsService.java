package com.dall.service;

import com.dall.config.MapsConfiguration;
import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class MapsService {
    private final MapsConfiguration mapsConfiguration;
    private GeoApiContext context;

    @Autowired
    public MapsService(
            MapsConfiguration mapsConfiguration) {
        this.mapsConfiguration = mapsConfiguration;
        this.initContext();
    }

    public void initContext() {
        this.context = new GeoApiContext.Builder()
                .apiKey(this.mapsConfiguration.getKey())
                .build();
    }

    public long computeDistance(LatLng adPosition) {
        LatLng basePosition = new LatLng(this.mapsConfiguration.getLatitude(), this.mapsConfiguration.getLongitude());
        DistanceMatrixApiRequest request = DistanceMatrixApi.newRequest(this.context);

        try {
            DistanceMatrix result = request.origins(basePosition)
                    .destinations(adPosition)
                    .mode(this.mapsConfiguration.getMode())
                    .units(this.mapsConfiguration.getUnits())
                    .await();
            log.info(String.valueOf(result));
            return result.rows[0].elements[0].distance.inMeters;
        } catch (InterruptedException | ApiException | IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
