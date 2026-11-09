package com.grandhorizon.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Room {
    private static final BigDecimal NORMAL_HOURLY_RATE = new BigDecimal("1000.00");
    private static final BigDecimal STANDARD_HOURLY_RATE = new BigDecimal("2000.00");
    private static final BigDecimal LUXURY_HOURLY_RATE = new BigDecimal("3000.00");
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

    public BigDecimal calculatePrice(int hours) {
        validateDuration(hours);
        return getHourlyRate().multiply(BigDecimal.valueOf(hours));
    }

    protected void validateDuration(int hours) {
        if (hours < 1) {
            throw new IllegalArgumentException("Duration must be at least one hour.");
        }
    }

    public BigDecimal getHourlyRate() {
        return hourlyRateForType(roomType);
    }

    public static BigDecimal hourlyRateForType(String roomType) {
        if (roomType == null) {
            throw new IllegalArgumentException("Choose a valid room type.");
        }
        return switch (roomType.toUpperCase(java.util.Locale.ROOT)) {
            case "NORMAL" -> NORMAL_HOURLY_RATE;
            case "STANDARD" -> STANDARD_HOURLY_RATE;
            case "LUXURY" -> LUXURY_HOURLY_RATE;
            default -> throw new IllegalArgumentException("Choose a valid room type.");
        };
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
    public String getMeals() {
        return mealOptions.stream()
                .map(com.grandhorizon.util.BookingPricing::mealDisplayName)
                .collect(java.util.stream.Collectors.joining(", "));
    }
    public void setMeals(String meals) {
        if (meals == null || meals.isBlank()) {
            setMealOptions(List.of());
            return;
        }
        setMealOptions(Arrays.stream(meals.split("\\s*[|,]\\s*"))
                .map(com.grandhorizon.util.BookingPricing::normalizeMeal)
                .distinct()
                .toList());
    }
    public boolean isBreakfastAvailable() { return mealOptions.contains("BREAKFAST"); }
    public boolean isLunchAvailable() { return mealOptions.contains("LUNCH"); }
    public boolean isDinnerAvailable() { return mealOptions.contains("DINNER"); }
}
