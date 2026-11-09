package com.grandhorizon.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private long id;
    private String roomNumber;
    private String roomType = "STANDARD";
    private String description;
    private int capacity;
    private BigDecimal pricePerNight;
    private String amenities;
    private boolean available;
    private String imageFileName = "";
    private List<String> mealOptions = new ArrayList<>();

    public Room() {
    }

    public Room(long id, String roomNumber, String description, int capacity,
                BigDecimal pricePerNight, String amenities, boolean available) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.description = description;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
        this.amenities = amenities;
        this.available = available;
    }

    public BigDecimal calculatePrice(int days) {
        validateDays(days);
        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }

    protected void validateDays(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("Days must be at least one.");
        }
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getImageFileName() {
        return imageFileName.matches("[0-9a-f]{32}\\.png") ? imageFileName : "";
    }
    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName == null ? "" : imageFileName;
    }
    public List<String> getMealOptions() { return List.copyOf(mealOptions); }
    public void setMealOptions(List<String> mealOptions) {
        this.mealOptions = mealOptions == null ? new ArrayList<>() : new ArrayList<>(mealOptions);
    }
    public boolean isBreakfastAvailable() { return mealOptions.contains("BREAKFAST"); }
    public boolean isLunchAvailable() { return mealOptions.contains("LUNCH"); }
    public boolean isDinnerAvailable() { return mealOptions.contains("DINNER"); }
}
