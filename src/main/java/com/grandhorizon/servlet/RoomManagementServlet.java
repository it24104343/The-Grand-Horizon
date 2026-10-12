package com.grandhorizon.servlet;

import com.grandhorizon.dao.RoomDAO;
import com.grandhorizon.model.LuxuryRoom;
import com.grandhorizon.model.Room;
import com.grandhorizon.model.StandardRoom;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@WebServlet("/admin/rooms")
public class RoomManagementServlet extends HttpServlet {
    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        loadRooms(request);
        request.getRequestDispatcher("/WEB-INF/views/admin-rooms.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        try {
            boolean changed = switch (action == null ? "" : action) {
                case "create" -> roomDAO.create(readRoom(request, 0));
                case "update" -> roomDAO.update(readRoom(request, parseId(request.getParameter("id"))));
                case "delete" -> roomDAO.delete(parseId(request.getParameter("id")));
                default -> throw new InvalidRoomException("Choose a valid room-management action.");
            };
            if (!changed) {
                throw new InvalidRoomException("That room number is already in use or the room no longer exists.");
            }
            response.sendRedirect(request.getContextPath() + "/admin/rooms?updated=true");
        } catch (InvalidRoomException e) {
            request.setAttribute("error", e.getMessage());
            loadRooms(request);
            request.getRequestDispatcher("/WEB-INF/views/admin-rooms.jsp").forward(request, response);
        } catch (IOException e) {
            throw new ServletException("Unable to save room changes.", e);
        }
    }

    private void loadRooms(HttpServletRequest request) throws ServletException {
        try {
            List<Room> rooms = roomDAO.findAll();
            request.setAttribute("rooms", rooms);
        } catch (IOException e) {
            throw new ServletException("Unable to load rooms for administration.", e);
        }
    }

    private Room readRoom(HttpServletRequest request, long id) throws InvalidRoomException {
        String roomNumber = value(request, "roomNumber");
        String roomType = value(request, "roomType").toUpperCase(Locale.ROOT);
        String description = value(request, "description");
        String amenities = value(request, "amenities");
        String capacityText = value(request, "capacity");
        String priceText = value(request, "pricePerNight");

        if (roomNumber.isBlank() || roomNumber.length() > 20
                || description.isBlank() || description.length() > 500
                || amenities.isBlank() || amenities.length() > 500) {
            throw new InvalidRoomException("Enter a room number, description (up to 500 characters), and amenities (up to 500 characters).");
        }
        if (!roomType.equals("STANDARD") && !roomType.equals("LUXURY")) {
            throw new InvalidRoomException("Choose Standard or Luxury for the room type.");
        }

        try {
            int capacity = Integer.parseInt(capacityText);
            BigDecimal price = new BigDecimal(priceText).setScale(2, RoundingMode.UNNECESSARY);
            if (capacity < 1 || capacity > 50 || price.signum() <= 0) {
                throw new InvalidRoomException("Capacity must be from 1 to 50 and the nightly price must be greater than zero.");
            }
            Room room = roomType.equals("LUXURY")
                    ? new LuxuryRoom(id, roomNumber, description, capacity, price, amenities,
                            request.getParameter("available") != null)
                    : new StandardRoom(id, roomNumber, description, capacity, price, amenities,
                            request.getParameter("available") != null);
            room.setRoomType(roomType);
            return room;
        } catch (NumberFormatException | ArithmeticException e) {
            throw new InvalidRoomException("Enter a whole-number capacity and a valid price with no more than two decimal places.");
        }
    }

    private long parseId(String value) throws InvalidRoomException {
        try {
            long id = Long.parseLong(value);
            if (id < 1) {
                throw new InvalidRoomException("Choose a valid room.");
            }
            return id;
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidRoomException("Choose a valid room.");
        }
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private static class InvalidRoomException extends Exception {
        private InvalidRoomException(String message) {
            super(message);
        }
    }
}
