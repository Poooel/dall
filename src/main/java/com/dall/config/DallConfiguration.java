package com.dall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Data
@Configuration
@ConfigurationProperties(prefix = "dall")
public class DallConfiguration {
    private Set<Integer> cityCentreDistricts;
    private Set<Integer> suburbDistricts;
    private String linksRange;
    private int startingCellCityCentre;
    private int startingCellSuburb;
    private int endingCellCityCentre;
    private int endingCellSuburb;
    private String startingColumnCityCentre;
    private String startingColumnSuburb;
    private String endingColumnCityCentre;
    private String endingColumnSuburb;
    private String cityCentreSheetName;
    private String suburbSheetName;
}
