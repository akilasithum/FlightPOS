package com.pos.flightpos.objects;

import java.io.Serializable;

public class SoldItem implements Serializable{
    private String itemId;
    private String itemDesc;
    private String quantity;
    private String price;
    private String equipmentNo;
    private String drawer;
    private String priceBeforeDiscount;
    private String discount;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getEquipmentNo() {
        return equipmentNo;
    }

    public void setEquipmentNo(String equipmentNo) {
        this.equipmentNo = equipmentNo;
    }

    public String getDrawer() {
        return drawer;
    }

    public void setDrawer(String drawer) {
        this.drawer = drawer;
    }

    public String getPriceBeforeDiscount() {
        return priceBeforeDiscount;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public void setPriceBeforeDiscount(String priceBeforeDiscount) {
        this.priceBeforeDiscount = priceBeforeDiscount;
    }

    @Override
    public String toString() {
        return itemDesc;
    }
}
