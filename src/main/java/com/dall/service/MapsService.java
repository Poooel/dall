package com.dall.service;

import com.dall.config.MapsConfiguration;
import com.dall.error.MapsException;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class MapsService {
    private final MapsConfiguration mapsConfiguration;
    private GeoApiContext context;
    private LatLng baseCoordinates;

    MapsService(MapsConfiguration mapsConfiguration) {
        this.mapsConfiguration = mapsConfiguration;
        this.baseCoordinates = new LatLng(
            this.mapsConfiguration.getLatitude(),
            this.mapsConfiguration.getLongitude()
        );
        this.initContext();
    }

    public void initContext() {
        this.context = new GeoApiContext.Builder().apiKey(this.mapsConfiguration.getKey()).build();
    }

    private TravelMode getTravelMode(boolean isInCityCentre) {
        return isInCityCentre
            ? this.mapsConfiguration.getCityTravelMode() :
            this.mapsConfiguration.getSuburbTravelMode();
    }

    String buildMapsUrl(LatLng destination, boolean isInCityCentre) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
            .scheme("http")
            .host("www.google.com")
            .path("/maps/dir/")
            .queryParam("api", "1")
            .queryParam("origin", this.baseCoordinates.toString())
            .queryParam("destination", destination)
            .queryParam("travelmode", this.getTravelMode(isInCityCentre))
            .buildAndExpand();
        return uriComponents.encode().toUriString();
    }

    String computeDuration(LatLng adCoordinates, boolean isInCityCentre) {
        DistanceMatrixApiRequest request = DistanceMatrixApi.newRequest(this.context);

        try {
            DistanceMatrix result = request.origins(this.baseCoordinates)
                .destinations(adCoordinates)
                .mode(this.getTravelMode(isInCityCentre))
                .units(this.mapsConfiguration.getUnits())
                .await();

            DistanceMatrixElement pathInfos = result.rows[0].elements[0];
            if (pathInfos.duration != null) {
                return result.rows[0].elements[0].duration.humanReadable;
            } else {
                throw new MapsException("No path available from this address");
            }
        } catch (InterruptedException | ApiException | IOException | MapsException e) {
            log.error(e.getMessage());
            return "0 min";
        }
    }
}
