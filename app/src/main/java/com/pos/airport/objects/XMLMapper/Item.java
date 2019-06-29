package com.pos.airport.objects.XMLMapper;

public class Item {

    private String itemNo;
    private String itemCode;
    private String itemName;
    private String itemHHC;
    private String category;
    private String catCode;
    private String catlogNo;
    private String price;
    private String paxDiscPrice;
    private String staffDiscPrice;
    private String delist;
    private String dfsrOrder;
    private String scPrice;
    private String baseCurrency;
    private String basePrice;
    private String secondCurrency;
    private String secondPrice;
    private String activeDate;
    private String quantity;

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemHHC() {
        return itemHHC;
    }

    public void setItemHHC(String itemHHC) {
        this.itemHHC = itemHHC;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCatCode() {
        return catCode;
    }

    public void setCatCode(String catCode) {
        this.catCode = catCode;
    }

    public String getCatlogNo() {
        return catlogNo;
    }

    public void setCatlogNo(String catlogNo) {
        this.catlogNo = catlogNo;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPaxDiscPrice() {
        return paxDiscPrice;
    }

    public void setPaxDiscPrice(String paxDiscPrice) {
        this.paxDiscPrice = paxDiscPrice;
    }

    public String getStaffDiscPrice() {
        return staffDiscPrice;
    }

    public void setStaffDiscPrice(String staffDiscPrice) {
        this.staffDiscPrice = staffDiscPrice;
    }

    public String getDelist() {
        return delist;
    }

    public void setDelist(String delist) {
        this.delist = delist;
    }

    public String getDfsrOrder() {
        return dfsrOrder;
    }

    public void setDfsrOrder(String dfsrOrder) {
        this.dfsrOrder = dfsrOrder;
    }

    public String getScPrice() {
        return scPrice;
    }

    public void setScPrice(String scPrice) {
        this.scPrice = scPrice;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(String basePrice) {
        this.basePrice = basePrice;
    }

    public String getSecondCurrency() {
        return secondCurrency;
    }

    public void setSecondCurrency(String secondCurrency) {
        this.secondCurrency = secondCurrency;
    }

    public String getSecondPrice() {
        return secondPrice;
    }

    public void setSecondPrice(String secondPrice) {
        this.secondPrice = secondPrice;
    }

    public String getActiveDate() {
        return activeDate;
    }

    public void setActiveDate(String activeDate) {
        this.activeDate = activeDate;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return itemName;
    }
}
