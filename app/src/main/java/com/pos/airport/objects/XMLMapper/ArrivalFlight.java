package com.pos.airport.objects.XMLMapper;
import java.util.Date;

public class ArrivalFlight {

    private int arrFlightId;
    private String flightNo;
    private String flightTime;
    private String airline;
    private String from;
    private String belt;
    private String gate;
    private String status;

    public int getArrFlightId() {
        return arrFlightId;
    }

    public void setArrFlightId(int arrFlightId) {
        this.arrFlightId = arrFlightId;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public String getFlightTime() {
        return flightTime;
    }

    public void setFlightTime(String flightTime) {
        this.flightTime = flightTime;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getBelt() {
        return belt;
    }

    public void setBelt(String belt) {
        this.belt = belt;
    }

    public String getGate() {
        return gate;
    }

    public void setGate(String gate) {
        this.gate = gate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
