package com.grandhorizon.util;

import com.grandhorizon.model.Room;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class BookingPricing {
    // Utility class for pricing and validation. It calculates the amount for a stay and ensures
    // meal selections match only the options available for a particular room type.
    private static final BigDecimal BREAKFAST_RATE = new BigDecimal("500.00");
    private static final BigDecimal LUNCH_RATE = new BigDecimal("800.00");
    private static final BigDecimal DINNER_RATE = new BigDecimal("1000.00");
    private static final List<String> MEAL_ORDER = List.of("BREAKFAST", "LUNCH", "DINNER");

    private BookingPricing() {
    }

    public static BigDecimal calculate(Room room, int durationHours) {
        return calculate(room, durationHours, List.of());
    }

    public static BigDecimal calculate(Room room, int durationHours, List<String> selectedMeals) {
        // Total price is the hourly room rate multiplied by the stay length, plus any chosen meal charges.
        if (durationHours < 1 || durationHours > 72) {
            throw new IllegalArgumentException("Duration must be from 1 to 72 hours.");
        }
        BigDecimal mealTotal = BigDecimal.ZERO;
        for (String meal : validateSelectedMeals(room, selectedMeals)) {
            mealTotal = mealTotal.add(mealRate(meal));
        }
        return room.getHourlyRate().multiply(BigDecimal.valueOf(durationHours))
                .add(mealTotal).setScale(2, RoundingMode.HALF_UP);
    }

    public static List<String> validateSelectedMeals(Room room, List<String> selectedMeals) {
        // Keep only meals that the room actually supports and preserve the standard breakfast/lunch/dinner order.
        Set<String> offeredMeals = new LinkedHashSet<>();
        for (String meal : room.getMealOptions()) {
            offeredMeals.add(normalizeMeal(meal));
        }

        Set<String> requestedMeals = new LinkedHashSet<>();
        if (selectedMeals != null) {
            for (String meal : selectedMeals) {
                String normalized = normalizeMeal(meal);
                if (!offeredMeals.contains(normalized)) {
                    throw new IllegalArgumentException("One or more selected meals are not available for this room.");
                }
                requestedMeals.add(normalized);
            }
        }

        List<String> validatedMeals = new ArrayList<>();
        for (String meal : MEAL_ORDER) {
            if (requestedMeals.contains(meal)) {
                validatedMeals.add(meal);
            }
        }
        return List.copyOf(validatedMeals);
    }

    public static String normalizeMeal(String meal) {
        if (meal == null) {
            throw new IllegalArgumentException("Choose only the listed meal options.");
        }
        String normalized = meal.trim().toUpperCase(Locale.ROOT);
        if (!MEAL_ORDER.contains(normalized)) {
            throw new IllegalArgumentException("Choose only the listed meal options.");
        }
        return normalized;
    }

    public static BigDecimal mealRate(String meal) {
        return switch (normalizeMeal(meal)) {
            case "BREAKFAST" -> BREAKFAST_RATE;
            case "LUNCH" -> LUNCH_RATE;
            case "DINNER" -> DINNER_RATE;
            default -> throw new IllegalArgumentException("Choose only the listed meal options.");
        };
    }

    public static String mealDisplayName(String meal) {
        return switch (normalizeMeal(meal)) {
            case "BREAKFAST" -> "Breakfast";
            case "LUNCH" -> "Lunch";
            case "DINNER" -> "Dinner";
            default -> throw new IllegalArgumentException("Choose only the listed meal options.");
        };
    }
}
