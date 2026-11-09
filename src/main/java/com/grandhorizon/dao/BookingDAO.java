package com.grandhorizon.dao;

import com.grandhorizon.model.Booking;
import com.grandhorizon.util.FileStorageUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class BookingDAO {
    public boolean isAvailable(long roomId, String roomNumber,
                               LocalDateTime checkIn, LocalDateTime checkOut)
            throws IOException {
        synchronized (FileStorageUtil.bookingsLock()) {
            return readBookings(FileStorageUtil.getBookingsFile()).stream()
                    .filter(booking -> booking.getRoomId() == roomId
                            || (booking.getRoomId() == 0
                            && booking.getRoomNumber().equalsIgnoreCase(roomNumber)))
                    .filter(booking -> "ACTIVE".equals(booking.getStatus()))
                    .noneMatch(booking -> overlaps(booking, checkIn, checkOut));
        }
    }

    public boolean createIfAvailable(Booking booking) throws IOException {
        synchronized (FileStorageUtil.bookingsLock()) {
            Path file = FileStorageUtil.getBookingsFile();
            List<Booking> bookings = readBookings(file);
            boolean occupied = bookings.stream()
                    .filter(existing -> existing.getRoomId() == booking.getRoomId()
                            || (existing.getRoomId() == 0
                            && existing.getRoomNumber().equalsIgnoreCase(booking.getRoomNumber())))
                    .filter(existing -> "ACTIVE".equals(existing.getStatus()))
                    .anyMatch(existing -> overlaps(existing, booking.getCheckIn(), booking.getCheckOut()));
            if (occupied) {
                return false;
            }
            long nextId = bookings.stream().mapToLong(Booking::getId).max().orElse(0L) + 1;
            booking.setId(nextId);
            bookings.add(booking);
            writeBookings(file, bookings);
            return true;
        }
    }

    public List<Booking> findForUser(long userId) throws IOException {
        synchronized (FileStorageUtil.bookingsLock()) {
            return readBookings(FileStorageUtil.getBookingsFile()).stream()
                    .filter(booking -> booking.getUserId() == userId)
                    .sorted(Comparator.comparing(Booking::getCheckIn).reversed())
                    .toList();
        }
    }

    public List<Booking> findCurrent() throws IOException {
        synchronized (FileStorageUtil.bookingsLock()) {
            return readBookings(FileStorageUtil.getBookingsFile()).stream()
                    .filter(booking -> "ACTIVE".equals(booking.getStatus()))
                    .sorted(Comparator.comparing(Booking::getCheckIn))
                    .toList();
        }
    }

    public List<Booking> findCancelled() throws IOException {
        synchronized (FileStorageUtil.bookingsLock()) {
            return readBookings(FileStorageUtil.getBookingsFile()).stream()
                    .filter(booking -> "CANCELLED".equals(booking.getStatus()))
                    .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
                    .toList();
        }
    }

    public int countCurrent() throws IOException {
        synchronized (FileStorageUtil.bookingsLock()) {
            return (int) readBookings(FileStorageUtil.getBookingsFile()).stream()
                    .filter(booking -> "ACTIVE".equals(booking.getStatus()))
                    .count();
        }
    }

    public Optional<Booking> cancelUpcoming(long bookingId, long userId) throws IOException {
        synchronized (FileStorageUtil.bookingsLock()) {
            Path file = FileStorageUtil.getBookingsFile();
            List<Booking> bookings = readBookings(file);
            Optional<Booking> candidate = bookings.stream()
                    .filter(booking -> booking.getId() == bookingId && booking.getUserId() == userId)
                    .filter(Booking::isCancellable)
                    .findFirst();
            if (candidate.isEmpty()) {
                return Optional.empty();
            }
            candidate.get().setStatus("CANCELLED");
            writeBookings(file, bookings);
            return candidate;
        }
    }

    private boolean overlaps(Booking existing, LocalDateTime checkIn, LocalDateTime checkOut) {
        return existing.getCheckIn().isBefore(checkOut) && existing.getCheckOut().isAfter(checkIn);
    }

    private List<Booking> readBookings(Path file) throws IOException {
        List<Booking> bookings = new ArrayList<>();
        List<List<String>> rows = FileStorageUtil.readCsv(file);
        for (int index = 0; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            if (index == 0 && !row.isEmpty()
                    && ("bookingId".equalsIgnoreCase(row.get(0)) || "username".equalsIgnoreCase(row.get(0)))) {
                continue;
            }
            try {
                if (row.size() == 5) {
                    bookings.add(readLegacyBooking(row, bookings.size() + 1L));
                } else if (row.size() == 13) {
                    bookings.add(readPreviousBooking(row));
                } else if (row.size() == 14) {
                    bookings.add(readBooking(row));
                } else {
                    throw new IOException("Invalid booking CSV row " + (index + 1) + " in " + file);
                }
            } catch (DateTimeParseException | IllegalArgumentException e) {
                throw new IOException("Invalid value in booking CSV row " + (index + 1) + " in " + file, e);
            }
        }
        return bookings;
    }

    private Booking readLegacyBooking(List<String> row, long id) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setUserId(0);
        booking.setGuestName(row.get(0));
        booking.setGuestEmail(row.get(0));
        booking.setRoomId(0);
        booking.setRoomNumber(row.get(1));
        booking.setCheckIn(parseLegacyDateTime(row.get(2), LocalTime.of(14, 0)));
        booking.setCheckOut(parseLegacyDateTime(row.get(3), LocalTime.of(11, 0)));
        booking.setMealOptions(List.of());
        booking.setPoolAccess(false);
        booking.setTotalAmount(new BigDecimal(row.get(4)));
        booking.setStatus("ACTIVE");
        booking.setCreatedAt(booking.getCheckIn());
        return booking;
    }

    private LocalDateTime parseLegacyDateTime(String value, LocalTime defaultTime) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            return LocalDate.parse(value).atTime(defaultTime);
        }
    }

    private Booking readPreviousBooking(List<String> row) {
        Booking booking = new Booking();
        booking.setId(Long.parseLong(row.get(0)));
        booking.setUserId(Long.parseLong(row.get(1)));
        booking.setGuestName(row.get(2));
        booking.setGuestEmail(row.get(3));
        booking.setRoomId(Long.parseLong(row.get(4)));
        booking.setRoomNumber(row.get(5));
        booking.setCheckIn(LocalDateTime.parse(row.get(6)));
        booking.setCheckOut(LocalDateTime.parse(row.get(7)));
        booking.setMealOptions(row.get(8).isBlank()
                ? List.of()
                : List.of(row.get(8).split("\\|")));
        booking.setPoolAccess(Boolean.parseBoolean(row.get(9)));
        booking.setTotalAmount(new BigDecimal(row.get(10)));
        booking.setStatus(row.get(11).toUpperCase(Locale.ROOT));
        booking.setCreatedAt(LocalDateTime.parse(row.get(12)));
        return booking;
    }

    private Booking readBooking(List<String> row) {
        Booking booking = new Booking();
        booking.setId(Long.parseLong(row.get(0)));
        booking.setUserId(Long.parseLong(row.get(1)));
        booking.setGuestName(row.get(2));
        booking.setGuestEmail(row.get(3));
        booking.setRoomId(Long.parseLong(row.get(4)));
        booking.setRoomNumber(row.get(5));
        booking.setCheckIn(LocalDate.parse(row.get(6)).atTime(LocalTime.parse(row.get(7))));
        long durationHours = Long.parseLong(row.get(8));
        if (durationHours < 1 || durationHours > 72) {
            throw new IllegalArgumentException("Invalid booking duration.");
        }
        booking.setCheckOut(booking.getCheckIn().plusHours(durationHours));
        booking.setMealOptions(row.get(9).isBlank()
                ? List.of()
                : List.of(row.get(9).split("\\|")));
        booking.setPoolAccess(Boolean.parseBoolean(row.get(10)));
        booking.setTotalAmount(new BigDecimal(row.get(11)));
        booking.setStatus(row.get(12).toUpperCase(Locale.ROOT));
        booking.setCreatedAt(LocalDateTime.parse(row.get(13)));
        return booking;
    }

    private void writeBookings(Path file, List<Booking> bookings) throws IOException {
        List<String> rows = new ArrayList<>();
        rows.add("bookingId,userId,guestName,guestEmail,roomId,roomNumber,date,startTime,durationHours,mealOptions,poolAccess,totalAmount,status,createdAt");
        for (Booking booking : bookings) {
            rows.add(String.join(",",
                    Long.toString(booking.getId()),
                    Long.toString(booking.getUserId()),
                    FileStorageUtil.toCsvField(booking.getGuestName()),
                    FileStorageUtil.toCsvField(booking.getGuestEmail()),
                    Long.toString(booking.getRoomId()),
                    FileStorageUtil.toCsvField(booking.getRoomNumber()),
                    booking.getCheckIn().toLocalDate().toString(),
                    booking.getCheckIn().toLocalTime().toString(),
                    Long.toString(booking.getDurationHours()),
                    FileStorageUtil.toCsvField(String.join("|", booking.getMealOptions())),
                    Boolean.toString(booking.isPoolAccess()),
                    booking.getTotalAmount().toPlainString(),
                    booking.getStatus(),
                    booking.getCreatedAt().toString()));
        }
        FileStorageUtil.writeAtomically(file, rows);
    }
}
