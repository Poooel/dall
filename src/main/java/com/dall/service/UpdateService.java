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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UpdateService {
    private final Sheets.Spreadsheets spreadsheet;
    private final GoogleCredentialsConfiguration googleCredentialsConfiguration;
    private final DallConfiguration dallConfiguration;
    private final ScrapService scrapService;
    private final WriteService writeService;
    private final DateService dateService;

    @Autowired
    public UpdateService(
        Sheets sheets,
        GoogleCredentialsConfiguration googleCredentialsConfiguration,
        DallConfiguration dallConfiguration,
        ScrapService scrapService,
        WriteService writeService,
        DateService dateService
    ) {
        this.spreadsheet = sheets.spreadsheets();
        this.googleCredentialsConfiguration = googleCredentialsConfiguration;
        this.dallConfiguration = dallConfiguration;
        this.scrapService = scrapService;
        this.writeService = writeService;
        this.dateService = dateService;
    }

    public void updateSpreadSheet() {
        List<Ad> adsFromCityCentre = parseRawLines(getLinesFromCityCentre());
        List<Ad> adsFromSuburb = parseRawLines(getLinesFromSuburb());

        List<Ad> updatedAdsFromCityCentre = updateAds(adsFromCityCentre);
        List<Ad> updatedAdsFromSuburb = updateAds(adsFromSuburb);

        List<List<Object>> serializedAdsFromCityCentre = serializeAds(updatedAdsFromCityCentre);
        List<List<Object>> serializedAdsFromSuburb = serializeAds(updatedAdsFromSuburb);

        writeService.rawWriteToSheet(serializedAdsFromCityCentre, getRangeForCityCentre());
        log.info("Updated {} ad{} for city centre.", serializedAdsFromCityCentre.size(), serializedAdsFromCityCentre.size() > 1 ? "s" : "");
        writeService.rawWriteToSheet(serializedAdsFromSuburb, getRangeForSuburb());
        log.info("Updated {} ad{} for suburb.", serializedAdsFromSuburb.size(), serializedAdsFromSuburb.size() > 1 ? "s" : "");
    }

    private List<Ad> updateAds(List<Ad> ads) {
        for (Ad ad : ads) {
            ScrapService.ScrapedAd updatedAd = scrapService.load(ad.getShortenedLink());

            ad.setDateAdded(dateService.getNow());

            if (updatedAd.getRemoved()) {
                ad.setRemoved(true);
                log.warn("Ad has been removed from website: {}", ad.getShortenedLink());
            } else {
                ad.setPrice(updatedAd.getPrice());
                ad.setLastModified(updatedAd.getLastModified());
                ad.setViews(updatedAd.getViews());
            }
        }

        return ads;
    }

    private String getRangeForCityCentre() {
        return String.format(
            "%s!%s%d:%s%d",
            dallConfiguration.getCityCentreSheetName(),
            dallConfiguration.getStartingColumnCityCentre(),
            dallConfiguration.getStartingCellCityCentre(),
            dallConfiguration.getEndingColumnCityCentre(),
            dallConfiguration.getEndingCellCityCentre()
        );
    }

    private String getRangeForSuburb() {
        return String.format(
            "%s!%s%d:%s%d",
            dallConfiguration.getSuburbSheetName(),
            dallConfiguration.getStartingColumnSuburb(),
            dallConfiguration.getStartingCellSuburb(),
            dallConfiguration.getEndingColumnSuburb(),
            dallConfiguration.getEndingCellSuburb()
        );
    }

    private List<List<Object>> getLinesFromCityCentre() {
        return getLines(getRangeForCityCentre());
    }

    private List<List<Object>> getLinesFromSuburb() {
        return getLines(getRangeForSuburb());
    }

    @SneakyThrows
    private List<List<Object>> getLines(String range) {
        ValueRange result = spreadsheet
            .values()
            .get(googleCredentialsConfiguration.getSpreadsheetId(), range)
            .execute();

        return result.getValues();
    }

    private List<Ad> parseRawLines(List<List<Object>> rawLines) {
        return rawLines
            .stream()
            .filter(rawLine -> !rawLine.get(0).toString().isEmpty())
            .map(rawLine ->
                new Ad(
                    rawLine.get(0).toString(),
                        rawLine.get(1).toString(),
                        rawLine.get(2).toString(),
                        rawLine.get(3).toString(),
                        rawLine.get(4).toString(),
                        rawLine.get(5).toString(),
                        rawLine.get(6).toString(),
                        rawLine.get(7).toString(),
                        rawLine.get(8).toString(),
                        rawLine.get(9).toString(),
                        rawLine.get(10).toString(),
                        rawLine.get(11).toString(),
                        Boolean.parseBoolean(rawLine.get(12).toString())
                )
            )
            .collect(Collectors.toList());
    }

    private List<List<Object>> serializeAds(List<Ad> ads) {
        List<List<Object>> serializedAds = new ArrayList<>();

        for (Ad ad : ads) {
            serializedAds.add(
                Arrays.asList(
                        ad.getShortenedLink(),
                        ad.getAddress(),
                        ad.getDistrict(),
                        ad.getLeaseTime(),
                        ad.getBathrooms(),
                        ad.getLastModified(),
                        ad.getViews(),
                        ad.getDuration(),
                        ad.getPrice(),
                        ad.getPer(),
                        ad.getDateAdded(),
                        ad.isRemoved()
                )
            );
        }

        return serializedAds;
    }
}
