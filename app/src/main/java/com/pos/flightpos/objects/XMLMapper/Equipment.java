package com.pos.flightpos.objects.XMLMapper;

public class Equipment {

    private String equipmentNo;
    private String equipmentDesc;
    private String equipmentType;
    private String drawerPrefix;
    private String noOfDrawers;
    private String kitCode;

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }

    public String getEquipmentDesc() {
        return equipmentDesc;
    }

    public void setEquipmentDesc(String equipmentDesc) {
        this.equipmentDesc = equipmentDesc;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getDrawerPrefix() {
        return drawerPrefix;
    }

    public void setDrawerPrefix(String drawerPrefix) {
        this.drawerPrefix = drawerPrefix;
    }

    public String getNoOfDrawers() {
        return noOfDrawers;
    }

    public void setNoOfDrawers(String noOfDrawers) {
        this.noOfDrawers = noOfDrawers;
    }

    public String getKitCode() {
        return kitCode;
    }

    public void setKitCode(String kitCode) {
        this.kitCode = kitCode;
    }
}
