package com.dall.entity;

import lombok.Value;

import java.time.LocalDate;

@Value
public class Ad {
    /**
     * Shorted link to the ad on daft.ie
     */
    private String shortenedLink;

    /**
     * The address of the appartment
     */
    private String address;

    /**
     * The district of the appartment
     */
    private String district;

    /**
     * The minimum lease time possible
     */
    private String leaseTime;

    /**
     * Number of bathrooms in the appartment
     */
    private String bathrooms;

    /**
     * The last date the ad was modified on daft.ie
     */
    private String lastModified;

    /**
     * The number of views the ad has
     */
    private String views;

    /**
     * The price to rent the appartment
     */
    private String price;

    /**
     * The price is either per week or per month
     */
    private String per;

    /**
     * The date when the ad was added to the Sheets document
     */
    private String dateAdded;

    /**
     * Has the ad been removed from the website or the let has been agreed
     */
    private boolean removed;
}
