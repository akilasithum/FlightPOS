package com.pos.flightpos.objects.XMLMapper;

public class POSFlight {

    private String flightId;
    private String flightName;
    private String flightDate;
    private String flightFrom;
    private String flightTo;
    private String eClassPaxCount;
    private String bClassPaxCount;

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

    public String getFlightDate() {
        return flightDate;
    }

    public void setFlightDate(String flightDate) {
        this.flightDate = flightDate;
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

    public String geteClassPaxCount() {
        return eClassPaxCount;
    }

    public void seteClassPaxCount(String eClassPaxCount) {
        this.eClassPaxCount = eClassPaxCount;
    }

    public String getbClassPaxCount() {
        return bClassPaxCount;
    }

    public void setbClassPaxCount(String bClassPaxCount) {
        this.bClassPaxCount = bClassPaxCount;
    }
}
