package com.pos.airport.objects;

public class AcceptPreOrder {

    String orderNumber;
    String paxName;
    String flightNumber;
    String flightDate;
    String flightSector;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getPaxName() {
        return paxName;
    }

    public void setPaxName(String paxName) {
        this.paxName = paxName;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getFlightDate() {
        return flightDate;
    }

    public void setFlightDate(String flightDate) {
        this.flightDate = flightDate;
    }

    public String getFlightSector() {
        return flightSector;
    }

    public void setFlightSector(String flightSector) {
        this.flightSector = flightSector;
    }
}
