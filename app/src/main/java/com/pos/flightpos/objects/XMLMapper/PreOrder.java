package com.pos.flightpos.objects.XMLMapper;

import java.io.Serializable;

public class PreOrder implements Serializable {

    private String PNR;
    private String customerName;
    private String serviceType;
    private String itemCategory;
    private String itemId;
    private String quantity;
    private String delivered;

    public String getPNR() {
        return PNR;
    }

    public void setPNR(String PNR) {
        this.PNR = PNR;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getDelivered() {
        return delivered;
    }

    public void setDelivered(String delivered) {
        this.delivered = delivered;
    }
}
