package com.pos.flightpos.objects.XMLMapper;

import java.util.Date;

public class SIFDetails {
    private String sifNo;
    private String deviceId;
    private String packedFor;
    private String packedTime;
    private String crewOpenedTime;
    private String crewClosedTime;
    private String programs;

    public String getSifNo() {
        return sifNo;
    }

    public void setSifNo(String sifNo) {
        this.sifNo = sifNo;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPackedFor() {
        return packedFor;
    }

    public void setPackedFor(String packedFor) {
        this.packedFor = packedFor;
    }

    public String getPackedTime() {
        return packedTime;
    }

    public void setPackedTime(String packedTime) {
        this.packedTime = packedTime;
    }

    public String getCrewOpenedTime() {
        return crewOpenedTime;
    }

    public void setCrewOpenedTime(String crewOpenedTime) {
        this.crewOpenedTime = crewOpenedTime;
    }

    public String getCrewClosedTime() {
        return crewClosedTime;
    }

    public void setCrewClosedTime(String crewClosedTime) {
        this.crewClosedTime = crewClosedTime;
    }

    public String getPrograms() {
        return programs;
    }

    public void setPrograms(String programs) {
        this.programs = programs;
    }
}
