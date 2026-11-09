package com.grandhorizon.dao;

import com.grandhorizon.model.LuxuryRoom;
import com.grandhorizon.model.Room;
import com.grandhorizon.model.StandardRoom;
import com.grandhorizon.util.FileStorageUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RoomDAO {
    public List<Room> findAvailableRooms() throws IOException {
        synchronized (FileStorageUtil.roomsLock()) {
            return readRooms(FileStorageUtil.getRoomsFile()).stream()
                    .filter(Room::isAvailable)
                    .sorted(Comparator.comparing(Room::getPricePerNight))
                    .toList();
        }
    }

    public List<Room> findAll() throws IOException {
        synchronized (FileStorageUtil.roomsLock()) {
            return List.copyOf(readRooms(FileStorageUtil.getRoomsFile()));
        }
    }

    public Optional<Room> findById(long id) throws IOException {
        synchronized (FileStorageUtil.roomsLock()) {
            return readRooms(FileStorageUtil.getRoomsFile()).stream()
                    .filter(room -> room.getId() == id)
                    .findFirst();
        }
    }

    public boolean create(Room room) throws IOException {
        synchronized (FileStorageUtil.roomsLock()) {
            Path roomsFile = FileStorageUtil.getRoomsFile();
            List<Room> rooms = readRooms(roomsFile);
            if (rooms.stream().anyMatch(existing ->
                    existing.getRoomNumber().equalsIgnoreCase(room.getRoomNumber()))) {
                return false;
            }
            long nextId = rooms.stream().mapToLong(Room::getId).max().orElse(0L) + 1;
            room.setId(nextId);
            rooms.add(room);
            writeRooms(roomsFile, rooms);
            return true;
        }
    }

    public boolean update(Room updatedRoom) throws IOException {
        synchronized (FileStorageUtil.roomsLock()) {
            Path roomsFile = FileStorageUtil.getRoomsFile();
            List<Room> rooms = readRooms(roomsFile);
            boolean duplicateRoomNumber = rooms.stream().anyMatch(room ->
                    room.getId() != updatedRoom.getId()
                            && room.getRoomNumber().equalsIgnoreCase(updatedRoom.getRoomNumber()));
            if (duplicateRoomNumber) {
                return false;
            }
            for (int index = 0; index < rooms.size(); index++) {
                if (rooms.get(index).getId() == updatedRoom.getId()) {
                    rooms.set(index, updatedRoom);
                    writeRooms(roomsFile, rooms);
                    return true;
                }
            }
            return false;
        }
    }

    public boolean delete(long id) throws IOException {
        synchronized (FileStorageUtil.roomsLock()) {
            Path roomsFile = FileStorageUtil.getRoomsFile();
            List<Room> rooms = readRooms(roomsFile);
            boolean removed = rooms.removeIf(room -> room.getId() == id);
            if (removed) {
                writeRooms(roomsFile, rooms);
            }
            return removed;
        }
    }

    private List<Room> readRooms(Path file) throws IOException {
        List<Room> rooms = new ArrayList<>();
        List<List<String>> rows = FileStorageUtil.readCsv(file);
        for (int index = 0; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            if (index == 0 && !row.isEmpty() && "id".equalsIgnoreCase(row.get(0))) {
                continue;
            }
            if (row.size() != 8 && row.size() != 10) {
                throw new IOException("Invalid room CSV row " + (index + 1) + " in " + file);
            }
            try {
                long id = Long.parseLong(row.get(0));
                String roomNumber = row.get(1);
                String roomType = row.get(2).toUpperCase(java.util.Locale.ROOT);
                String description = row.get(3);
                int capacity = Integer.parseInt(row.get(4));
                BigDecimal price = Room.hourlyRateForType(roomType);
                String amenities = row.get(6);
                boolean available = Boolean.parseBoolean(row.get(7));
                String imageFileName = row.size() == 10 ? row.get(8) : "";
                Room room = "LUXURY".equals(roomType)
                        ? new LuxuryRoom(id, roomNumber, description, capacity, price, amenities, available)
                        : new StandardRoom(id, roomNumber, description, capacity, price, amenities, available);
                room.setRoomType(roomType);
                room.setImageFileName(imageFileName);
                if (row.size() == 8) {
                    room.setMealOptions(List.of("BREAKFAST", "LUNCH", "DINNER"));
                } else {
                    room.setMeals(row.get(9));
                }
                rooms.add(room);
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid value in room CSV row " + (index + 1) + " in " + file, e);
            }
        }
        return rooms;
    }

    private void writeRooms(Path file, List<Room> rooms) throws IOException {
        List<String> rows = new ArrayList<>();
        rows.add("id,roomNumber,roomType,description,capacity,pricePerHour,amenities,available,imageFileName,mealOptions");
        for (Room room : rooms) {
            rows.add(String.join(",",
                    Long.toString(room.getId()),
                    FileStorageUtil.toCsvField(room.getRoomNumber()),
                    FileStorageUtil.toCsvField(room.getRoomType()),
                    FileStorageUtil.toCsvField(room.getDescription()),
                    Integer.toString(room.getCapacity()),
                    FileStorageUtil.toCsvField(room.getHourlyRate().toPlainString()),
                    FileStorageUtil.toCsvField(room.getAmenities()),
                    Boolean.toString(room.isAvailable()),
                    FileStorageUtil.toCsvField(room.getImageFileName()),
                    FileStorageUtil.toCsvField(room.getMeals())));
        }
        FileStorageUtil.writeAtomically(file, rows);
    }
}
