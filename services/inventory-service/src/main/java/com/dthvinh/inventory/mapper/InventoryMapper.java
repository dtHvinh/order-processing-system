package com.dthvinh.inventory.mapper;

import com.dthvinh.inventory.api.InventoryResponse;
import com.dthvinh.inventory.model.Inventory;

public class InventoryMapper {
    public static InventoryResponse toResponse(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        return new InventoryResponse(inventory.getProductId(), inventory.getAvailable(), inventory.getReserved());
    }
}
