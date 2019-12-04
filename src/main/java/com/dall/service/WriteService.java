package com.dall.service;

import com.dall.config.DallConfiguration;
import com.dall.config.GoogleCredentialsConfiguration;
import com.dall.entity.Ad;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class WriteService {
    private final Sheets.Spreadsheets spreadsheet;
    private final GoogleCredentialsConfiguration googleCredentialsConfiguration;
    private final RangeService rangeService;
    private final DallConfiguration dallConfiguration;

    @Autowired
    public WriteService(
        Sheets sheets,
        GoogleCredentialsConfiguration googleCredentialsConfiguration,
        RangeService rangeService,
        DallConfiguration dallConfiguration
    ) {
        this.spreadsheet = sheets.spreadsheets();
        this.googleCredentialsConfiguration = googleCredentialsConfiguration;
        this.rangeService = rangeService;
        this.dallConfiguration = dallConfiguration;
    }

    void writeToSheet(Ad ad) {
        int districtNumber = Integer.parseInt(ad.getDistrict().split(" ")[1]);

        if (dallConfiguration.getCityCentreDistricts().contains(districtNumber)) {
            writeToCityCentreSheet(ad);
        } else {
            writeToSuburbSheet(ad);
        }
    }

    private void writeToCityCentreSheet(Ad ad) {
        write(ad, rangeService.getWritingRangeForCityCentre());
    }

    private void writeToSuburbSheet(Ad ad) {
        write(ad, rangeService.getWritingRangeForSuburb());
    }

    @SneakyThrows
    private void write(Ad ad, String range) {
        List<List<Object>> body = createBodyFromAd(ad);
        ValueRange valueRange = new ValueRange().setValues(body);

        spreadsheet.values()
            .update(googleCredentialsConfiguration.getSpreadsheetId(), range, valueRange)
            .setValueInputOption("USER_ENTERED")
            .execute();

        log.info("Added {}.", ad.getShortenedLink());
    }

    private List<List<Object>> createBodyFromAd(Ad ad) {
        return Collections.singletonList(
            Arrays.asList(
                ad.getShortenedLink(),
                ad.getAddress(),
                ad.getDistrict(),
                ad.getLeaseTime(),
                ad.getBathrooms(),
                ad.getLastModified(),
                ad.getViews(),
                ad.getPrice(),
                ad.getPer(),
                ad.getDateAdded()
            )
        );
    }
}
