package com.analysys.track.model;

import java.io.Serializable;

/**
 * 定位信息
 */
public class LocationInfo  implements Serializable {

    private static final long serialVersionUID = 1L;
    private String CollectionTime;
    private String GeographyLocation;

    public String getCollectionTime() {
        return CollectionTime;
    }

    public String getGeographyLocation() {
        return GeographyLocation;
    }

    public void setCollectionTime(String collectionTime) {
        CollectionTime = collectionTime;
    }

    public void setGeographyLocation(String geographyLocation) {
        GeographyLocation = geographyLocation;
    }
}
