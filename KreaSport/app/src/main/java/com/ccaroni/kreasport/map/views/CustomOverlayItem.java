package com.ccaroni.kreasport.map.views;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * Created by Master on 05/04/2017.
 */

public class CustomOverlayItem extends OverlayItem {

    private String id;
    private String raceId;
    private boolean primary;

    public CustomOverlayItem(String aTitle, String aSnippet, IGeoPoint aGeoPoint, String id, String raceId) {
        super(aTitle, aSnippet, aGeoPoint);
        this.id = id;
        this.raceId = raceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRaceId() {
        return raceId;
    }

    public void setRaceId(String raceId) {
        this.raceId = raceId;
    }

    /**
     * Whether this item represents a checkpoint or a RealmRace.
     * If it is a RealmRace, it does not have a checkpointId
     * @return
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * Sets this item as a primary item.
     * As such it DOES NOT have a checkpointId
     * @param primary
     * @return
     */
    public CustomOverlayItem setPrimary(boolean primary) {
        this.primary = primary;
        return this;
    }

    @Override
    public String toString() {
        return "CustomOverlayItem{" +
                "id='" + id + '\'' +
                "title'" + getTitle() + '\'' +
                ", raceId='" + raceId + '\'' +
                ", primary=" + primary +
                '}';
    }
}
