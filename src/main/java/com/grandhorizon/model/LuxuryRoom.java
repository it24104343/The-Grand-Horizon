package com.grandhorizon.model;

import java.math.BigDecimal;

// LuxuryRoom is a premium room type with the highest hourly rate and luxury features.
public class LuxuryRoom extends Room {
    public LuxuryRoom(long id, String roomNumber, String description, int capacity,
                      BigDecimal pricePerNight, String amenities, boolean available) {
        super(id, roomNumber, description, capacity, pricePerNight, amenities, available);
        setRoomType("LUXURY");
    }
}
