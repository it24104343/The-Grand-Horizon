package com.grandhorizon.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;

public class Booking {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private long id;
    private long userId;
    private String guestName;
    private String guestEmail;
    private long roomId;
    private String roomNumber;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private List<String> mealOptions = List.of();
    private boolean poolAccess;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public long getRoomId() { return roomId; }
    public void setRoomId(long roomId) { this.roomId = roomId; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public LocalDateTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDateTime checkIn) { this.checkIn = checkIn; }
    public LocalDateTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDateTime checkOut) { this.checkOut = checkOut; }
    public List<String> getMealOptions() { return List.copyOf(mealOptions); }
    public void setMealOptions(List<String> mealOptions) {
        this.mealOptions = mealOptions == null ? List.of() : List.copyOf(mealOptions);
    }
    public boolean isPoolAccess() { return poolAccess; }
    public void setPoolAccess(boolean poolAccess) { this.poolAccess = poolAccess; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public long getDurationHours() {
        return java.time.Duration.between(checkIn, checkOut).toHours();
    }

    public long getDaysRemaining() {
        if (isCompleted()) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), checkOut.toLocalDate()));
    }

    public boolean isCompleted() {
        return checkOut != null && !checkOut.isAfter(LocalDateTime.now());
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isCancellable() {
        return "ACTIVE".equals(status) && checkIn != null && checkIn.isAfter(LocalDateTime.now());
    }

    public String getMealsDisplay() {
        return mealOptions.isEmpty() ? "None" : mealOptions.stream()
                .map(com.grandhorizon.util.BookingPricing::mealDisplayName)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    public String getBookingDate() {
        return checkIn.toLocalDate().toString();
    }

    public String getStartTime() {
        return checkIn.toLocalTime().format(TIME_FORMAT);
    }

    public String getStartTimeAmPm() {
        return checkIn.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH));
    }

    public String getTimeSlot() {
        return checkIn.toLocalTime().format(TIME_FORMAT) + " – "
                + checkOut.toLocalTime().format(TIME_FORMAT);
    }
}
