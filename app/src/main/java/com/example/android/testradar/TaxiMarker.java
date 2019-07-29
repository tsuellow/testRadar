package com.example.android.testradar;

import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.MarkerItem;

import java.util.Date;

public class TaxiMarker extends MarkerItem {
    private int taxiId;
    private String taxiComment;
    private float acc;
    private Date time;


    public TaxiMarker(Object uid, String title, String description, GeoPoint geoPoint, int taxiId, String taxiComment) {
        super(uid, title, description, geoPoint);
        this.taxiId= taxiId;
        this.taxiComment= taxiComment;
    }

    public TaxiMarker(int taxiId, double lat, double lon, float acc, Date time){
        super(taxiId,null,null,new GeoPoint(lat,lon));
        this.acc=acc;
        this.time=time;
        this.taxiId=taxiId;
    }

    public int getTaxiId() {
        return taxiId;
    }

    public void setTaxiId(int taxiId) {
        this.taxiId = taxiId;
    }

    public String getTaxiComment() {
        return taxiComment;
    }

    public void setTaxiComment(String taxiComment) {
        this.taxiComment = taxiComment;
    }
}
