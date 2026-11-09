package com.grandhorizon.model;

import java.math.BigDecimal;

public class LuxuryRoom extends Room {
    public LuxuryRoom(long id, String roomNumber, String description, int capacity,
                      BigDecimal pricePerNight, String amenities, boolean available) {
        super(id, roomNumber, description, capacity, pricePerNight, amenities, available);
        setRoomType("LUXURY");
    }
}
