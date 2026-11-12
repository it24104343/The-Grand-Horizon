package com.grandhorizon.servlet;

import com.grandhorizon.dao.BookingDAO;
import com.grandhorizon.dao.RoomDAO;
import com.grandhorizon.model.Room;
import com.grandhorizon.util.BookingPricing;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@WebServlet("/booking/availability")
// BookingAvailabilityServlet checks whether a room is free for the requested booking slot and returns JSON for the UI.
public class BookingAvailabilityServlet extends HttpServlet {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        try {
            long roomId = Long.parseLong(request.getParameter("roomId"));
            LocalDate date = LocalDate.parse(request.getParameter("date"));
            LocalTime startTime = LocalTime.parse(request.getParameter("startTime"));
            int durationHours = Integer.parseInt(request.getParameter("durationHours"));
            if (durationHours < 1 || durationHours > 72) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"available\":false,\"message\":\"Duration must be from 1 to 72 hours.\"}");
                return;
            }
            LocalDateTime checkIn = LocalDateTime.of(date, startTime);
            LocalDateTime checkOut = checkIn.plusHours(durationHours);
            if (!checkIn.isAfter(LocalDateTime.now()) || !checkOut.isAfter(checkIn)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"available\":false,\"message\":\"Choose a future date and time.\"}");
                return;
            }
            Optional<Room> room = roomDAO.findById(roomId);
            if (room.isEmpty() || !room.get().isAvailable()) {
                response.getWriter().write("{\"available\":false,\"message\":\"This room is not available for booking.\"}");
                return;
            }
            boolean available = bookingDAO.isAvailable(
                    roomId, room.get().getRoomNumber(), checkIn, checkOut);
            if (!available) {
                response.getWriter().write("{\"available\":false,\"message\":\"Room is already booked for this time.\"}");
                return;
            }
            List<String> selectedMeals = request.getParameterValues("mealOptions") == null
                    ? List.of()
                    : Arrays.asList(request.getParameterValues("mealOptions"));
            String total;
            try {
                total = BookingPricing.calculate(room.get(), durationHours,
                        BookingPricing.validateSelectedMeals(room.get(), selectedMeals)).toPlainString();
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"available\":false,\"message\":\""
                        + e.getMessage() + "\"}");
                return;
            }
            response.getWriter().write("{\"available\":true,\"message\":\"Room is available this time.\","
                    + "\"total\":\"" + total + "\"}");
        } catch (NumberFormatException | DateTimeParseException | NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"available\":false,\"message\":\"Choose valid dates and times.\"}");
        }
    }
}
