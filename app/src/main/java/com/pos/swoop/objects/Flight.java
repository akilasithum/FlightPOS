package com.pos.swoop.objects;

import java.util.List;

public class Flight {

    private String flightId;
    private String flightName;
    private String flightFrom;
    private String flightTo;
    private String sectorStr;
    private List<Sector> sectorList;

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

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

    public String getSectorStr() {
        return sectorStr;
    }

    public void setSectorStr(String sectorStr) {
        this.sectorStr = sectorStr;
    }

    public List<Sector> getSectorList() {
        return sectorList;
    }

    public void setSectorList(List<Sector> sectorList) {
        this.sectorList = sectorList;
    }

    @Override
    public String toString() {
        if(flightName == null){
            return "";
        }
        return flightName;
    }
}
