package com.grandhorizon.model;

import java.math.BigDecimal;

public class StandardRoom extends Room {
    public StandardRoom(long id, String roomNumber, String description, int capacity,
                        BigDecimal pricePerNight, String amenities, boolean available) {
        super(id, roomNumber, description, capacity, pricePerNight, amenities, available);
    }

    @Override
    public BigDecimal calculatePrice(int days) {
        return super.calculatePrice(days);
    }
}
