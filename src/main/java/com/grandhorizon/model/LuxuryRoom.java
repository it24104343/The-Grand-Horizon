package com.grandhorizon.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LuxuryRoom extends Room {
    private static final BigDecimal LUXURY_SURCHARGE = new BigDecimal("1.20");

    public LuxuryRoom(long id, String roomNumber, String description, int capacity,
                      BigDecimal pricePerNight, String amenities, boolean available) {
        super(id, roomNumber, description, capacity, pricePerNight, amenities, available);
        setRoomType("LUXURY");
    }

    @Override
    public BigDecimal calculatePrice(int days) {
        return super.calculatePrice(days).multiply(LUXURY_SURCHARGE)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
