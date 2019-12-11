package com.dall.service;

import com.dall.config.MapsConfiguration;
import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MapsService {
    private final MapsConfiguration mapsConfiguration;
    private GeoApiContext context;

    @Autowired
    public MapsService(
            MapsConfiguration mapsConfiguration) {
        this.mapsConfiguration = mapsConfiguration;
    }

    public void initContext() {
        this.context = new GeoApiContext.Builder()
                .apiKey(this.mapsConfiguration.getKey())
                .build();
    }

    public long computeDistance(String adAddress) {
        DistanceMatrixApiRequest request = DistanceMatrixApi.newRequest(this.context);
        try {
            DistanceMatrix result = request.origins(adAddress)
                    .destinations(this.mapsConfiguration.getAddress())
                    .mode(TravelMode.valueOf(this.mapsConfiguration.getMode()))
                    .units(Unit.valueOf(this.mapsConfiguration.getUnits()))
                    .await();
            return result.rows[0].elements[0].distance.inMeters;
        } catch (InterruptedException | ApiException | IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
