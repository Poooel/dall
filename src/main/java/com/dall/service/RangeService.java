package com.dall.service;

import com.dall.config.DallConfiguration;
import com.dall.config.GoogleCredentialsConfiguration;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RangeService {
    private final Sheets.Spreadsheets spreadsheet;
    private final GoogleCredentialsConfiguration googleCredentialsConfiguration;
    private final DallConfiguration dallConfiguration;

    @Autowired
    public RangeService(
        Sheets sheets,
        GoogleCredentialsConfiguration googleCredentialsConfiguration,
        DallConfiguration dallConfiguration
    ) {
        this.spreadsheet = sheets.spreadsheets();
        this.googleCredentialsConfiguration = googleCredentialsConfiguration;
        this.dallConfiguration = dallConfiguration;
    }

    public String getWritingRangeForCityCentre() {
        return getWritingRange(dallConfiguration.getCityCentreSheetName());
    }

    public String getWritingRangeForSuburb() {
        return getWritingRange(dallConfiguration.getSuburbSheetName());
    }

    @SneakyThrows
    private String getWritingRange(String sheet) {
        ValueRange result = this.spreadsheet
            .values()
            .get(googleCredentialsConfiguration.getSpreadsheetId(),
                String.format("%s!%s%d:%s1000",
                    sheet,
                    dallConfiguration.getStartingColumnCityCentre(),
                    dallConfiguration.getStartingCellCityCentre(),
                    dallConfiguration.getStartingColumnCityCentre())).execute();

        int resultRange = result.getValues() != null ? result.getValues().size() : 0;
        int finalRangeValue = dallConfiguration.getStartingCellCityCentre() + resultRange;

        return String.format("%s!%s%d", sheet, dallConfiguration.getStartingColumnCityCentre(), finalRangeValue);
    }
}
