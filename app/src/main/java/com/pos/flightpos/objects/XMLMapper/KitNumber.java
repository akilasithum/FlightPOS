package com.pos.flightpos.objects.XMLMapper;

public class KitNumber {

    private String kitCode;
    private String kitDesc;
    private String serviceType;
    private String activeDate;
    private String noOfEq;
    private String noOfSeals;
    private String equipment;

    public String getKitCode() {
        return kitCode;
    }

    public void setKitCode(String kitCode) {
        this.kitCode = kitCode;
    }

    public String getKitDesc() {
        return kitDesc;
    }

    public void setKitDesc(String kitDesc) {
        this.kitDesc = kitDesc;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getActiveDate() {
        return activeDate;
    }

    public void setActiveDate(String activeDate) {
        this.activeDate = activeDate;
    }

    public String getNoOfEq() {
        return noOfEq;
    }

    public void setNoOfEq(String noOfEq) {
        this.noOfEq = noOfEq;
    }

    public String getNoOfSeals() {
        return noOfSeals;
    }

    public void setNoOfSeals(String noOfSeals) {
        this.noOfSeals = noOfSeals;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }
}
