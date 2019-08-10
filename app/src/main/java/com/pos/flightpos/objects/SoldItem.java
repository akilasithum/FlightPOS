package com.pos.flightpos.objects;

import java.io.Serializable;
import java.util.List;

public class SoldItem implements Serializable{
    private String itemId;
    private String itemDesc;
    private String quantity;
    private String price;
    private String equipmentNo;
    private String drawer;
    private String priceBeforeDiscount;
    private String discount;
    private String total;
    private String itemCategory;
    private List<ItemDrawer> itemDrawerList;

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

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public List<ItemDrawer> getItemDrawerList() {
        return itemDrawerList;
    }

    public void setItemDrawerList(List<ItemDrawer> itemDrawerList) {
        this.itemDrawerList = itemDrawerList;
    }

    @Override
    public String toString() {
        return itemDesc;
    }
}
