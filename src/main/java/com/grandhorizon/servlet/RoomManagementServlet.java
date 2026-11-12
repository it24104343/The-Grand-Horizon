package com.grandhorizon.servlet;

import com.grandhorizon.dao.RoomDAO;
import com.grandhorizon.model.LuxuryRoom;
import com.grandhorizon.model.Room;
import com.grandhorizon.model.StandardRoom;
import com.grandhorizon.util.BookingPricing;
import com.grandhorizon.util.RoomImageStorage;
import com.grandhorizon.util.RoomImageStorage.InvalidImageException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@WebServlet("/admin/rooms")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
// RoomManagementServlet lets the admin create, edit, and delete rooms, including image uploads.
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
        String newlyStoredImage = null;
        try {
            String action = request.getParameter("action");
            boolean changed;
            switch (action == null ? "" : action) {
                case "create" -> {
                    Room room = readRoom(request, 0);
                    newlyStoredImage = storeImage(request);
                    if (newlyStoredImage != null) {
                        room.setImageFileName(newlyStoredImage);
                    }
                    changed = roomDAO.create(room);
                }
                case "update" -> {
                    long id = parseId(request.getParameter("id"));
                    Room existing = roomDAO.findById(id)
                            .orElseThrow(() -> new InvalidRoomException("That room no longer exists."));
                    Room room = readRoom(request, id);
                    room.setImageFileName(existing.getImageFileName());
                    newlyStoredImage = storeImage(request);
                    if (newlyStoredImage != null) {
                        room.setImageFileName(newlyStoredImage);
                    }
                    changed = roomDAO.update(room);
                    if (changed && newlyStoredImage != null) {
                        deleteObsoleteImage(existing.getImageFileName());
                    }
                }
                case "delete" -> {
                    long id = parseId(request.getParameter("id"));
                    Room existing = roomDAO.findById(id)
                            .orElseThrow(() -> new InvalidRoomException("That room no longer exists."));
                    changed = roomDAO.delete(id);
                    if (changed) {
                        deleteObsoleteImage(existing.getImageFileName());
                    }
                }
                default -> throw new InvalidRoomException("Choose a valid room-management action.");
            }
            if (!changed) {
                throw new InvalidRoomException("That room number is already in use or the room no longer exists.");
            }
            response.sendRedirect(request.getContextPath() + "/admin/rooms?updated=true");
        } catch (InvalidRoomException e) {
            cleanupImage(newlyStoredImage, e);
            request.setAttribute("error", e.getMessage());
            loadRooms(request);
            request.getRequestDispatcher("/WEB-INF/views/admin-rooms.jsp").forward(request, response);
        } catch (IOException | ServletException e) {
            cleanupImage(newlyStoredImage, e);
            throw new ServletException("Unable to save room changes.", e);
        } catch (IllegalStateException e) {
            cleanupImage(newlyStoredImage, e);
            request.setAttribute("error", "Room images must be 5 MB or smaller.");
            loadRooms(request);
            request.getRequestDispatcher("/WEB-INF/views/admin-rooms.jsp").forward(request, response);
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
        Set<String> mealOptions = parseMealOptions(request.getParameterValues("mealOptions"));

        if (roomNumber.isBlank() || roomNumber.length() > 20
                || description.isBlank() || description.length() > 500
                || amenities.isBlank() || amenities.length() > 500) {
            throw new InvalidRoomException("Enter a room number, description (up to 500 characters), and amenities (up to 500 characters).");
        }
        if (!Set.of("NORMAL", "STANDARD", "LUXURY").contains(roomType)) {
            throw new InvalidRoomException("Choose Normal, Standard, or Luxury for the room type.");
        }

        try {
            int capacity = Integer.parseInt(capacityText);
            BigDecimal hourlyRate = Room.hourlyRateForType(roomType);
            if (capacity < 1 || capacity > 50) {
                throw new InvalidRoomException("Capacity must be from 1 to 50.");
            }
            Room room = roomType.equals("LUXURY")
                    ? new LuxuryRoom(id, roomNumber, description, capacity, hourlyRate, amenities,
                            request.getParameter("available") != null)
                    : new StandardRoom(id, roomNumber, description, capacity, hourlyRate, amenities,
                            request.getParameter("available") != null);
            room.setRoomType(roomType);
            room.setMealOptions(new ArrayList<>(mealOptions));
            return room;
        } catch (NumberFormatException e) {
            throw new InvalidRoomException("Enter a whole-number capacity.");
        }
    }

    private Set<String> parseMealOptions(String[] submittedMealOptions) throws InvalidRoomException {
        Set<String> mealOptions = new LinkedHashSet<>();
        if (submittedMealOptions == null) {
            return mealOptions;
        }
        try {
            for (String meal : submittedMealOptions) {
                mealOptions.add(BookingPricing.normalizeMeal(meal));
            }
        } catch (IllegalArgumentException e) {
            throw new InvalidRoomException("Choose only Breakfast, Lunch, or Dinner.");
        }
        return mealOptions;
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

    private String storeImage(HttpServletRequest request)
            throws IOException, ServletException, InvalidRoomException {
        try {
            Part image = request.getPart("roomImage");
            return RoomImageStorage.save(image);
        } catch (InvalidImageException e) {
            throw new InvalidRoomException(e.getMessage());
        } catch (IllegalStateException e) {
            throw new InvalidRoomException("Room images must be 5 MB or smaller.");
        }
    }

    private void cleanupImage(String fileName, Exception cause) {
        if (fileName == null) {
            return;
        }
        try {
            RoomImageStorage.delete(fileName);
        } catch (IOException e) {
            cause.addSuppressed(e);
            getServletContext().log("Unable to clean up an unused room image.", e);
        }
    }

    private void deleteObsoleteImage(String fileName) {
        try {
            RoomImageStorage.delete(fileName);
        } catch (IOException e) {
            getServletContext().log("Room changes were saved, but an obsolete room image could not be removed.", e);
        }
    }

    private static class InvalidRoomException extends Exception {
        private InvalidRoomException(String message) {
            super(message);
        }
    }
}
