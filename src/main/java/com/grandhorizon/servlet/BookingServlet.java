package com.grandhorizon.servlet;

import com.grandhorizon.dao.RoomDAO;
import com.grandhorizon.dao.UserDAO;
import com.grandhorizon.model.Room;
import com.grandhorizon.util.FileStorageUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@WebServlet("/booking")
public class BookingServlet extends HttpServlet {
    private final RoomDAO roomDAO = new RoomDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
        request.setCharacterEncoding("UTF-8");
        Room room = findRoom(request.getParameter("roomId"));
        if (room == null || !room.isAvailable()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The selected room is not available.");
            return;
        }

        LocalDate checkIn;
        LocalDate checkOut;
        try {
            checkIn = LocalDate.parse(request.getParameter("checkIn"));
            checkOut = LocalDate.parse(request.getParameter("checkOut"));
        } catch (DateTimeParseException | NullPointerException e) {
            request.setAttribute("error", "Enter valid check-in and check-out dates.");
            showBookingForm(request, response, room);
            return;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        if (days < 1) {
            request.setAttribute("error", "Check-out must be at least one day after check-in.");
            showBookingForm(request, response, room);
            return;
        }

        String username = resolveUsername(request);
        BigDecimal totalAmount = room.getPricePerNight()
                .multiply(BigDecimal.valueOf(days))
                .setScale(2, RoundingMode.HALF_UP);

        try {
            synchronized (FileStorageUtil.bookingsLock()) {
                String booking = String.join(",",
                        FileStorageUtil.toCsvField(username),
                        FileStorageUtil.toCsvField(room.getRoomNumber()),
                        checkIn.toString(),
                        checkOut.toString(),
                        totalAmount.toPlainString()) + System.lineSeparator();
                Files.writeString(FileStorageUtil.getBookingsFile(), booking, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            throw new ServletException("Unable to save the room booking.", e);
        }

        request.setAttribute("username", username);
        request.setAttribute("roomName", room.getRoomNumber());
        request.setAttribute("checkIn", checkIn);
        request.setAttribute("checkOut", checkOut);
        request.setAttribute("totalAmount", totalAmount);
        request.getRequestDispatcher("/booking-success.jsp").forward(request, response);
    }

    private Room findRoom(String roomId) throws ServletException {
        if (roomId == null || roomId.isBlank()) {
            return null;
        }
        try {
            long id = Long.parseLong(roomId);
            Optional<Room> room = roomDAO.findById(id);
            return room.orElse(null);
        } catch (NumberFormatException e) {
            return null;
        } catch (IOException e) {
            throw new ServletException("Unable to load the selected room.", e);
        }
    }

    private String resolveUsername(HttpServletRequest request) throws ServletException {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        String username = (String) session.getAttribute("username");
        if (username == null || username.isBlank()) {
            username = (String) session.getAttribute("email");
        }
        if (username != null && !username.isBlank()) {
            return username;
        }

        Object userId = session.getAttribute("userId");
        if (userId instanceof Long id) {
            try {
                return userDAO.findById(id)
                        .map(user -> user.getEmail())
                        .filter(email -> email != null && !email.isBlank())
                        .orElseThrow(() -> new ServletException("Unable to resolve the signed-in user."));
            } catch (IOException e) {
                throw new ServletException("Unable to resolve the signed-in user.", e);
            }
        }
        throw new ServletException("The signed-in session does not include a user identity.");
    }

    private void showBookingForm(HttpServletRequest request, HttpServletResponse response, Room room)
            throws ServletException, IOException {
        request.setAttribute("room", room);
        request.setAttribute("today", LocalDate.now());
        request.getRequestDispatcher("/book-room.jsp").forward(request, response);
    }
}
