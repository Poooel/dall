package com.dall.service;

import com.dall.config.DallConfiguration;
import com.dall.config.GoogleCredentialsConfiguration;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DuplicateService {
    private final Set<String> links;
    private final Sheets.Spreadsheets spreadsheet;
    private final GoogleCredentialsConfiguration googleCredentialsConfiguration;
    private final DallConfiguration dallConfiguration;

    @Autowired
    public DuplicateService(
        Sheets sheets,
        GoogleCredentialsConfiguration googleCredentialsConfiguration,
        DallConfiguration dallConfiguration
    ) {
        this.spreadsheet = sheets.spreadsheets();
        this.googleCredentialsConfiguration = googleCredentialsConfiguration;
        this.dallConfiguration = dallConfiguration;

        links = loadExistingLinks();
    }

    @SneakyThrows
    private Set<String> loadExistingLinks() {
        ValueRange resultFromCityCentre = this.spreadsheet
            .values()
            .get(
                googleCredentialsConfiguration.getSpreadsheetId(),
                String.format(
                    "%s!%s%d:%s%d",
                    dallConfiguration.getCityCentreSheetName(),
                    dallConfiguration.getStartingColumnCityCentre(),
                    dallConfiguration.getStartingCellCityCentre(),
                    dallConfiguration.getStartingColumnCityCentre(),
                    100
                )
            )
            .execute();

        ValueRange resultFromSuburb = this.spreadsheet
            .values()
            .get(
                googleCredentialsConfiguration.getSpreadsheetId(),
                String.format(
                    "%s!%s%d:%s%d",
                    dallConfiguration.getSuburbSheetName(),
                    dallConfiguration.getStartingColumnSuburb(),
                    dallConfiguration.getStartingCellSuburb(),
                    dallConfiguration.getStartingColumnSuburb(),
                    100
                )
            )
            .execute();

        return Stream.concat(
            resultFromCityCentre.getValues().stream(),
            resultFromSuburb.getValues().stream()
        )
            .flatMap(Collection::stream)
            .map(Object::toString)
            .collect(Collectors.toSet());
    }

    public boolean exists(String link) {
        return links.contains(link);
    }
}
