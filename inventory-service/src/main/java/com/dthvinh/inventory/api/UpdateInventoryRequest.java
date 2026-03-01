package com.dthvinh.inventory.api;

public class UpdateInventoryRequest {
    private int available;
    private int reserved;

    public UpdateInventoryRequest() {
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }
}
