package com.grandhorizon.servlet;

import com.grandhorizon.dao.UserDAO;
import com.grandhorizon.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

@WebServlet("/profile/update")
public class UpdateProfileServlet extends HttpServlet {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Long userId = (Long) request.getSession(false).getAttribute("userId");
        String firstName = value(request, "firstName");
        String lastName = value(request, "lastName");
        String email = value(request, "email");
        String phone = value(request, "phone");
        String password = request.getParameter("newPassword");

        if (firstName.isBlank() || firstName.length() > 80
                || lastName.isBlank() || lastName.length() > 80
                || email.length() > 254 || phone.length() > 30 || !EMAIL.matcher(email).matches()
                || (password != null && !password.isBlank()
                && (password.length() < 8 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72))) {
            request.setAttribute("error", "Review your details. A new password must be 8–72 UTF-8 bytes.");
            forwardProfile(request, response, userId);
            return;
        }

        try {
            Optional<User> current = userDAO.findById(userId);
            if (current.isEmpty()) {
                request.getSession(false).invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            String passwordToSave = password == null || password.isBlank() ? "" : password;
            if (userDAO.updateProfile(userId, firstName, lastName, email, phone, passwordToSave)) {
                request.getSession(false).setAttribute("profileNotice", "Your profile has been updated.");
                response.sendRedirect(request.getContextPath() + "/profile");
            } else {
                request.setAttribute("error", "That email address is already associated with an account, or the profile no longer exists.");
                forwardProfile(request, response, userId);
            }
        } catch (IOException e) {
            throw new ServletException("Unable to update user profile.", e);
        }
    }

    private void forwardProfile(HttpServletRequest request, HttpServletResponse response, long userId)
            throws ServletException, IOException {
        try {
            request.setAttribute("user", userDAO.findById(userId).orElseThrow());
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
        } catch (IOException e) {
            throw new ServletException("Unable to reload user profile.", e);
        }
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
