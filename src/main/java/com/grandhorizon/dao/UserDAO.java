package com.grandhorizon.dao;

import com.grandhorizon.model.Admin;
import com.grandhorizon.model.Guest;
import com.grandhorizon.model.User;
import com.grandhorizon.util.FileStorageUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class UserDAO {
    public boolean createGuest(Guest guest, String password) throws IOException {
        synchronized (FileStorageUtil.usersLock()) {
            Path usersFile = FileStorageUtil.getUsersFile();
            List<User> records = readUsers(usersFile);
            String email = normalizeEmail(guest.getEmail());
            if (records.stream().anyMatch(user -> normalizeEmail(user.getEmail()).equals(email))) {
                return false;
            }

            long nextId = records.stream().mapToLong(User::getId).max().orElse(0L) + 1;
            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            guest.setId(nextId);
            guest.setEmail(email);
            guest.setPasswordHash(passwordHash);
            guest.setRole("GUEST");
            records.add(guest);
            writeUsers(usersFile, records);
            return true;
        }
    }

    public Optional<User> authenticate(String email, String password) throws IOException {
        synchronized (FileStorageUtil.usersLock()) {
            String normalizedEmail = normalizeEmail(email);
            return readUsers(FileStorageUtil.getUsersFile()).stream()
                    .filter(user -> normalizeEmail(user.getEmail()).equals(normalizedEmail))
                    .filter(user -> BCrypt.checkpw(password, user.getPasswordHash()))
                    .findFirst();
        }
    }

    public Optional<User> findById(long id) throws IOException {
        synchronized (FileStorageUtil.usersLock()) {
            return readUsers(FileStorageUtil.getUsersFile()).stream()
                    .filter(user -> user.getId() == id)
                    .findFirst();
        }
    }

    public boolean updateProfile(long id, String firstName, String lastName,
                                 String email, String phone, String newPassword) throws IOException {
        synchronized (FileStorageUtil.usersLock()) {
            Path usersFile = FileStorageUtil.getUsersFile();
            List<User> users = readUsers(usersFile);
            String normalizedEmail = normalizeEmail(email);
            boolean emailInUse = users.stream()
                    .anyMatch(user -> user.getId() != id
                            && normalizeEmail(user.getEmail()).equals(normalizedEmail));
            if (emailInUse) {
                return false;
            }

            for (User user : users) {
                if (user.getId() == id) {
                    user.setFirstName(firstName.trim());
                    user.setLastName(lastName.trim());
                    user.setEmail(normalizedEmail);
                    user.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
                    if (newPassword != null && !newPassword.isBlank()) {
                        user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
                    }
                    writeUsers(usersFile, users);
                    return true;
                }
            }
            return false;
        }
    }

    public boolean deleteAccount(long id, String password) throws IOException {
        synchronized (FileStorageUtil.usersLock()) {
            Path usersFile = FileStorageUtil.getUsersFile();
            List<User> users = readUsers(usersFile);
            Optional<User> existing = users.stream().filter(user -> user.getId() == id).findFirst();
            if (existing.isEmpty() || !BCrypt.checkpw(password, existing.get().getPasswordHash())) {
                return false;
            }
            users.remove(existing.get());
            writeUsers(usersFile, users);
            return true;
        }
    }

    private List<User> readUsers(Path file) throws IOException {
        List<User> users = new ArrayList<>();
        List<List<String>> rows = FileStorageUtil.readCsv(file);
        for (int index = 0; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            if (index == 0 && !row.isEmpty() && "id".equalsIgnoreCase(row.get(0))) {
                continue;
            }
            if (row.size() != 7) {
                throw new IOException("Invalid user CSV row " + (index + 1) + " in " + file);
            }
            try {
                String role = row.get(6);
                User user = "ADMIN".equalsIgnoreCase(role) ? new Admin() : new Guest();
                user.setId(Long.parseLong(row.get(0)));
                user.setFirstName(row.get(1));
                user.setLastName(row.get(2));
                user.setEmail(row.get(3));
                user.setPasswordHash(row.get(4));
                user.setPhone(row.get(5).isEmpty() ? null : row.get(5));
                user.setRole(role);
                users.add(user);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid user ID in CSV row " + (index + 1) + " in " + file, e);
            }
        }
        return users;
    }

    private void writeUsers(Path file, List<User> users) throws IOException {
        List<String> rows = new ArrayList<>();
        rows.add("id,firstName,lastName,email,passwordHash,phone,role");
        for (User user : users) {
            rows.add(String.join(",",
                    Long.toString(user.getId()),
                    FileStorageUtil.toCsvField(user.getFirstName()),
                    FileStorageUtil.toCsvField(user.getLastName()),
                    FileStorageUtil.toCsvField(user.getEmail()),
                    FileStorageUtil.toCsvField(user.getPasswordHash()),
                    FileStorageUtil.toCsvField(user.getPhone()),
                    FileStorageUtil.toCsvField(user.getRole())));
        }
        FileStorageUtil.writeAtomically(file, rows);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
