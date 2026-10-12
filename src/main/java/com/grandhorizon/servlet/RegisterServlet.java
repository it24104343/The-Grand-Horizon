package com.grandhorizon.servlet;

import com.grandhorizon.dao.UserDAO;
import com.grandhorizon.model.Guest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String firstName = value(request, "firstName");
        String lastName = value(request, "lastName");
        String email = value(request, "email");
        String phone = value(request, "phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (firstName.isBlank() || firstName.length() > 80
                || lastName.isBlank() || lastName.length() > 80
                || email.length() > 254 || phone.length() > 30 || !EMAIL.matcher(email).matches()
                || password == null || password.length() < 8
                || !password.equals(confirmPassword)) {
            request.setAttribute("error", "Check your details. Passwords must match and be at least 8 characters.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }
        if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            request.setAttribute("error", "Password must be no longer than 72 UTF-8 bytes.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        Guest guest = new Guest();
        guest.setFirstName(firstName);
        guest.setLastName(lastName);
        guest.setEmail(email);
        guest.setPhone(phone);
        try {
            if (userDAO.createGuest(guest, password)) {
                jakarta.servlet.http.HttpSession session = request.getSession(true);
                session.setAttribute("userId", guest.getId());
                session.setAttribute("username", guest.getEmail());
                session.setAttribute("email", guest.getEmail());
                session.setAttribute("role", "User");
                response.sendRedirect(request.getContextPath() + "/rooms");
            } else {
                request.setAttribute("error", "An account with that email address already exists.");
                request.getRequestDispatcher("/register.jsp").forward(request, response);
            }
        } catch (IOException e) {
            throw new ServletException("Unable to register guest.", e);
        }
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
