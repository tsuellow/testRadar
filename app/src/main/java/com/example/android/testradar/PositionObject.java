package com.example.android.testradar;

import java.util.Date;

public class PositionObject {
    int userid;
    double lat;
    double lon;
    float acc;
    Date time;

    public PositionObject(int userid, double lat, double lon, float acc, Date time) {
        this.userid = userid;
        this.lat = lat;
        this.lon = lon;
        this.acc = acc;
        this.time = time;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public float getAcc() {
        return acc;
    }

    public void setAcc(float acc) {
        this.acc = acc;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
