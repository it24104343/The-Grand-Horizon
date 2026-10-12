package com.grandhorizon.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class FileStorageUtil {
    private static final String DATA_DIRECTORY_PROPERTY = "grandhorizon.data.dir";
    private static final Object USERS_LOCK = new Object();
    private static final Object ROOMS_LOCK = new Object();
    private static final Object BOOKINGS_LOCK = new Object();
    private static final String USERS_HEADER =
            "id,firstName,lastName,email,passwordHash,phone,role";
    private static final String INITIAL_ADMIN_PASSWORD_HASH =
            "$2a$12$bRF3dTKNEStnEXJB/E6RO.HZI5J3fKc0J4YekscZ6yuAoFmIgblHS";
    private static final String ROOMS_HEADER =
            "id,roomNumber,roomType,description,capacity,pricePerNight,amenities,available";
    private static final String[] INITIAL_ROOMS = {
            "1,101,STANDARD,\"A calm, thoughtfully appointed room for a restful stay.\",2,145.00,\"Queen bed, rain shower, city view\",true",
            "2,204,STANDARD,\"A spacious room with warm finishes and a comfortable sitting area.\",2,175.00,\"King bed, sitting area, work desk\",true",
            "3,501,LUXURY,\"An elevated suite with premium comforts and panoramic views.\",2,320.00,\"King bed, air conditioning, Wi-Fi, lounge, panoramic view\",true",
            "4,502,LUXURY,\"A generous suite designed for a quieter, more indulgent stay.\",4,410.00,\"Two beds, air conditioning, Wi-Fi, soaking tub, lounge\",true"
    };

    private FileStorageUtil() {
    }

    public static Path getDataDirectory() {
        String configuredDirectory = System.getProperty(DATA_DIRECTORY_PROPERTY);
        if (configuredDirectory != null && !configuredDirectory.isBlank()) {
            return Path.of(configuredDirectory).toAbsolutePath().normalize();
        }
        return Path.of(System.getProperty("user.home"), ".grand-horizon", "data")
                .toAbsolutePath().normalize();
    }

    public static Path getUsersFile() throws IOException {
        Path file = getDataDirectory().resolve("users.txt");
        synchronized (USERS_LOCK) {
            if (!Files.exists(file) || Files.size(file) == 0) {
                ensureCsvFile(file, USERS_HEADER, new String[]{
                        "1,Mahela,Pramuditha,mahelampramuditha@gmail.com,"
                                + INITIAL_ADMIN_PASSWORD_HASH + ",,ADMIN"
                });
            }
        }
        return file;
    }

    public static Path getRoomsFile() throws IOException {
        Path file = getDataDirectory().resolve("rooms.txt");
        synchronized (ROOMS_LOCK) {
            ensureCsvFile(file, ROOMS_HEADER, INITIAL_ROOMS);
        }
        return file;
    }

    public static Path getBookingsFile() throws IOException {
        Path file = getDataDirectory().resolve("bookings.txt");
        synchronized (BOOKINGS_LOCK) {
            ensureCsvFile(file, "username,room_name,check_in,check_out,total_amount", new String[0]);
        }
        return file;
    }

    public static Object usersLock() {
        return USERS_LOCK;
    }

    public static Object roomsLock() {
        return ROOMS_LOCK;
    }

    public static Object bookingsLock() {
        return BOOKINGS_LOCK;
    }

    public static List<List<String>> readCsv(Path file) throws IOException {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            List<String> record = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean quoted = false;
            boolean closedQuote = false;
            int value;
            while ((value = reader.read()) != -1) {
                char character = (char) value;
                if (quoted) {
                    if (character == '"') {
                        reader.mark(1);
                        int next = reader.read();
                        if (next == '"') {
                            field.append('"');
                        } else {
                            quoted = false;
                            closedQuote = true;
                            if (next != -1) {
                                reader.reset();
                            }
                        }
                    } else {
                        field.append(character);
                    }
                } else if (closedQuote) {
                    if (character == ',') {
                        record.add(field.toString());
                        field.setLength(0);
                        closedQuote = false;
                    } else if (character == '\n' || character == '\r') {
                        record.add(field.toString());
                        field.setLength(0);
                        closedQuote = false;
                        addRecord(records, record);
                        record = new ArrayList<>();
                        consumeLineFeed(reader, character);
                    } else {
                        throw new IOException("Malformed CSV in " + file + ": unexpected character after quote.");
                    }
                } else if (character == '"') {
                    if (field.length() != 0) {
                        throw new IOException("Malformed CSV in " + file + ": quote inside an unquoted field.");
                    }
                    quoted = true;
                } else if (character == ',') {
                    record.add(field.toString());
                    field.setLength(0);
                } else if (character == '\n' || character == '\r') {
                    record.add(field.toString());
                    field.setLength(0);
                    addRecord(records, record);
                    record = new ArrayList<>();
                    consumeLineFeed(reader, character);
                } else {
                    field.append(character);
                }
            }
            if (quoted) {
                throw new IOException("Malformed CSV in " + file + ": unterminated quoted field.");
            }
            if (closedQuote || field.length() > 0 || !record.isEmpty()) {
                record.add(field.toString());
                addRecord(records, record);
            }
        }
        return records;
    }

    public static String toCsvField(String value) {
        String safeValue = value == null ? "" : value;
        if (safeValue.indexOf(',') >= 0 || safeValue.indexOf('"') >= 0
                || safeValue.indexOf('\n') >= 0 || safeValue.indexOf('\r') >= 0) {
            return '"' + safeValue.replace("\"", "\"\"") + '"';
        }
        return safeValue;
    }

    public static void writeAtomically(Path destination, List<String> lines) throws IOException {
        Path temporary = Files.createTempFile(destination.getParent(), destination.getFileName().toString(), ".tmp");
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(temporary.toFile(), StandardCharsets.UTF_8))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void ensureCsvFile(Path file, String header, String[] initialRows) throws IOException {
        Files.createDirectories(file.getParent());
        if (!Files.exists(file) || Files.size(file) == 0) {
            List<String> lines = new ArrayList<>();
            lines.add(header);
            lines.addAll(List.of(initialRows));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile(), StandardCharsets.UTF_8))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }

    private static void addRecord(List<List<String>> records, List<String> record) {
        if (!(record.size() == 1 && record.get(0).isEmpty())) {
            records.add(record);
        }
    }

    private static void consumeLineFeed(BufferedReader reader, char character) throws IOException {
        if (character == '\r') {
            reader.mark(1);
            if (reader.read() != '\n') {
                reader.reset();
            }
        }
    }
}
