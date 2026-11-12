package com.grandhorizon.servlet;

import com.grandhorizon.dao.BookingDAO;
import com.grandhorizon.dao.RoomDAO;
import com.grandhorizon.dao.UserDAO;
import com.grandhorizon.model.Booking;
import com.grandhorizon.model.Room;
import com.grandhorizon.model.User;
import com.grandhorizon.util.BookingPricing;
import com.grandhorizon.util.CsrfTokenUtil;
import com.grandhorizon.util.EmailUtil;
import jakarta.mail.MessagingException;
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

@WebServlet("/booking")
public class BookingServlet extends HttpServlet {
    // Controller for the booking flow: it fetches room data, validates input, and decides whether
    // to show a booking form or save a new reservation before forwarding to the JSP view.
    private final RoomDAO roomDAO = new RoomDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // When a user opens the booking page, load the selected room and reject invalid or unavailable rooms.
        Room room = findRoom(request.getParameter("roomId"));
        if (room == null || !room.isAvailable()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The selected room is not available.");
            return;
        }
        showBookingForm(request, response, room);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Prevent cross-site submit attacks and validate that the form is still using a valid session.
        request.setCharacterEncoding("UTF-8");
        if (!CsrfTokenUtil.isValid(request.getSession(false), request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The booking form expired. Reload and try again.");
            return;
        }
        Long userId = (Long) request.getSession(false).getAttribute("userId");
        Room room = findRoom(request.getParameter("roomId"));
        if (room == null || !room.isAvailable()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The selected room is not available.");
            return;
        }

        // Convert the selected date and time inputs into a real booking window and enforce the valid stay duration.
        LocalDateTime checkIn;
        LocalDateTime checkOut;
        int durationHours;
        List<String> selectedMeals;
        try {
            LocalDate date = LocalDate.parse(request.getParameter("date"));
            LocalTime startTime = LocalTime.parse(request.getParameter("startTime"));
            durationHours = Integer.parseInt(request.getParameter("durationHours"));
            if (durationHours < 1 || durationHours > 72) {
                throw new IllegalArgumentException("Invalid duration");
            }
            checkIn = LocalDateTime.of(date, startTime);
            checkOut = checkIn.plusHours(durationHours);
        } catch (DateTimeParseException | IllegalArgumentException | NullPointerException e) {
            request.setAttribute("error", "Enter a valid date, start time, and duration from 1 to 72 hours.");
            showBookingForm(request, response, room);
            return;
        }
        try {
            // Only accept meal choices that are actually offered for this room, preserving a valid order.
            selectedMeals = BookingPricing.validateSelectedMeals(room,
                    request.getParameterValues("mealOptions") == null
                            ? List.of()
                            : Arrays.asList(request.getParameterValues("mealOptions")));
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            showBookingForm(request, response, room);
            return;
        }
        if (!checkIn.isAfter(LocalDateTime.now())) {
            request.setAttribute("error", "Choose a future date and start time.");
            showBookingForm(request, response, room);
            return;
        }
        try {
            // Confirm the current user is a real guest account before creating the reservation record.
            Optional<User> guest = userDAO.findById(userId);
            if (guest.isEmpty() || "ADMIN".equalsIgnoreCase(guest.get().getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "A guest account is required to make a booking.");
                return;
            }

            // Build a booking object from the validated form data and calculate the final amount for the stay.
            Booking booking = new Booking();
            booking.setUserId(userId);
            booking.setGuestName(guest.get().getFirstName() + " " + guest.get().getLastName());
            booking.setGuestEmail(guest.get().getEmail());
            booking.setRoomId(room.getId());
            booking.setRoomNumber(room.getRoomNumber());
            booking.setCheckIn(checkIn);
            booking.setCheckOut(checkOut);
            booking.setMealOptions(selectedMeals);
            booking.setPoolAccess(false);
            booking.setTotalAmount(BookingPricing.calculate(room, durationHours, selectedMeals));
            booking.setStatus("ACTIVE");
            booking.setCreatedAt(LocalDateTime.now());

            // Save the booking only if no active reservation overlaps the chosen room and time range.
            if (!bookingDAO.createIfAvailable(booking)) {
                request.setAttribute("availabilityMessage", "Room is already booked for this time.");
                request.setAttribute("available", false);
                request.setAttribute("availabilityClass", "booking-unavailable");
                showBookingForm(request, response, room);
                return;
            }

            // Email confirmation is optional and should not prevent the booking from being saved successfully.
            if (EmailUtil.isConfigured()) {
                try {
                    EmailUtil.sendBookingConfirmation(booking);
                } catch (MessagingException | IllegalStateException e) {
                    getServletContext().log("Booking " + booking.getId()
                            + " was saved, but its confirmation email could not be sent.", e);
                    request.setAttribute("emailNotice", "Your booking is confirmed, but we could not send the confirmation email.");
                }
            } else {
                getServletContext().log("Booking " + booking.getId()
                        + " was saved; configure the Gmail app password in EmailUtil to send confirmation email.");
                request.setAttribute("emailNotice", "Your booking is confirmed. Email notifications are not configured.");
            }

            request.setAttribute("booking", booking);
            request.getRequestDispatcher("/booking-success.jsp").forward(request, response);
        } catch (IOException e) {
            throw new ServletException("Unable to save the room booking.", e);
        }
    }

    private Room findRoom(String roomId) throws ServletException {
        if (roomId == null || roomId.isBlank()) {
            return null;
        }
        try {
            long id = Long.parseLong(roomId);
            return roomDAO.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        } catch (IOException e) {
            throw new ServletException("Unable to load the selected room.", e);
        }
    }

    private void showBookingForm(HttpServletRequest request, HttpServletResponse response, Room room)
            throws ServletException, IOException {
        // Re-render the booking JSP with the selected room, the fresh CSRF token, and any validation errors.
        request.setAttribute("room", room);
        request.setAttribute("csrfToken", CsrfTokenUtil.getOrCreate(request.getSession(true)));
        request.setAttribute("minimumDate", LocalDate.now());
        request.setAttribute("hourlyRate", room.getHourlyRate());
        List<String> submittedMeals = request.getParameterValues("mealOptions") == null
                ? List.of()
                : Arrays.asList(request.getParameterValues("mealOptions"));
        request.setAttribute("selectedBreakfast", submittedMeals.contains("BREAKFAST")
                && room.isBreakfastAvailable());
        request.setAttribute("selectedLunch", submittedMeals.contains("LUNCH")
                && room.isLunchAvailable());
        request.setAttribute("selectedDinner", submittedMeals.contains("DINNER")
                && room.isDinnerAvailable());
        request.getRequestDispatcher("/book-room.jsp").forward(request, response);
    }
}
