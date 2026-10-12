package com.grandhorizon.servlet;

import com.grandhorizon.util.FileStorageUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String identifier = request.getParameter("usernameOrEmail");
        if (identifier == null) {
            identifier = request.getParameter("email");
        }
        String password = request.getParameter("password");
        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            request.setAttribute("error", "Enter your username or email and password.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        try {
            Optional<Identity> authenticated = authenticate(identifier.trim(), password);
            if (authenticated.isEmpty()) {
                request.setAttribute("error", "Those credentials could not be verified.");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            Identity identity = authenticated.get();
            HttpSession session = request.getSession(true);
            session.setAttribute("username", identity.username());
            session.setAttribute("email", identity.email());
            session.setAttribute("role", identity.role());
            if (identity.userId() != null) {
                session.setAttribute("userId", identity.userId());
            }

            String destination = "Admin".equals(identity.role())
                    ? "/admin-dashboard.jsp"
                    : "/rooms";
            response.sendRedirect(request.getContextPath() + destination);
        } catch (IOException | IllegalArgumentException e) {
            throw new ServletException("Unable to authenticate user.", e);
        }
    }

    private Optional<Identity> authenticate(String identifier, String password) throws IOException {
        synchronized (FileStorageUtil.usersLock()) {
            String normalizedIdentifier = identifier.toLowerCase(Locale.ROOT);
            Path usersFile = FileStorageUtil.getUsersFile();
            List<List<String>> rows = FileStorageUtil.readCsv(usersFile);
            for (int index = 0; index < rows.size(); index++) {
                List<String> row = rows.get(index);
                if (isHeader(row)) {
                    continue;
                }
                if (row.size() == 4) {
                    String username = row.get(0).trim();
                    String email = row.get(1).trim();
                    if (matches(normalizedIdentifier, username) || matches(normalizedIdentifier, email)) {
                        if (!password.equals(row.get(2))) {
                            return Optional.empty();
                        }
                        String role = normalizeRole(row.get(3));
                        if (role == null) {
                            throw new IOException("Unsupported role in users.txt row " + (index + 1) + ".");
                        }
                        return Optional.of(new Identity(username, email, role, null));
                    }
                } else if (row.size() == 7) {
                    String email = row.get(3).trim();
                    if (!matches(normalizedIdentifier, email)) {
                        continue;
                    }
                    String storedPassword = row.get(4);
                    if (isBcryptHash(storedPassword)) {
                        if (!BCrypt.checkpw(password, storedPassword)) {
                            return Optional.empty();
                        }
                    } else {
                        if (!constantTimeEquals(password, storedPassword)) {
                            return Optional.empty();
                        }
                        row.set(4, BCrypt.hashpw(password, BCrypt.gensalt(12)));
                        writeUsers(usersFile, rows);
                    }
                    String role = normalizeRole(row.get(6));
                    if (role == null) {
                        throw new IOException("Unsupported role in users.txt row " + (index + 1) + ".");
                    }
                    try {
                        return Optional.of(new Identity(email, email, role, Long.parseLong(row.get(0))));
                    } catch (NumberFormatException e) {
                        throw new IOException("Invalid user ID in users.txt row " + (index + 1) + ".", e);
                    }
                } else {
                    throw new IOException("Invalid users.txt row " + (index + 1) + ".");
                }
            }
            return Optional.empty();
        }
    }

    private boolean isBcryptHash(String value) {
        return value != null && value.matches("\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}");
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (actual == null) {
            return false;
        }
        return java.security.MessageDigest.isEqual(
                expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                actual.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private void writeUsers(Path file, List<List<String>> rows) throws IOException {
        List<String> lines = new ArrayList<>();
        for (List<String> row : rows) {
            lines.add(row.stream().map(FileStorageUtil::toCsvField)
                    .collect(java.util.stream.Collectors.joining(",")));
        }
        FileStorageUtil.writeAtomically(file, lines);
    }

    private boolean isHeader(List<String> row) {
        return !row.isEmpty()
                && ("username".equalsIgnoreCase(row.get(0)) || "id".equalsIgnoreCase(row.get(0)));
    }

    private boolean matches(String normalizedIdentifier, String candidate) {
        return candidate.toLowerCase(Locale.ROOT).equals(normalizedIdentifier);
    }

    private String normalizeRole(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return "Admin";
        }
        if ("USER".equalsIgnoreCase(role) || "GUEST".equalsIgnoreCase(role)) {
            return "User";
        }
        return null;
    }

    private record Identity(String username, String email, String role, Long userId) {
    }
}
