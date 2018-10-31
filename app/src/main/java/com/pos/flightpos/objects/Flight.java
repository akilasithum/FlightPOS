package com.pos.flightpos.objects;

import com.pos.flightpos.objects.XMLMapper.Sector;

import java.util.List;

public class Flight {

    private String flightName;
    private String flightFrom;
    private String flightTo;
    private String sectorStr;
    private List<Sector> sectorList;

    public String getFlightName() {
        return flightName;
    }

    public void setFlightName(String flightName) {
        this.flightName = flightName;
    }

    public String getFlightFrom() {
        return flightFrom;
    }

    public void setFlightFrom(String flightFrom) {
        this.flightFrom = flightFrom;
    }

    public String getFlightTo() {
        return flightTo;
    }

    public void setFlightTo(String flightTo) {
        this.flightTo = flightTo;
    }

    public List<Sector> getSectorList() {
        return sectorList;
    }

    public void setSectorList(List<Sector> sectorList) {
        this.sectorList = sectorList;
    }

    public String getSectorStr() {
        return sectorStr;
    }

    public void setSectorStr(String sectorStr) {
        this.sectorStr = sectorStr;
    }
}
