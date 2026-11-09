package com.grandhorizon.servlet;

import com.grandhorizon.dao.BookingDAO;
import com.grandhorizon.dao.UserDAO;
import com.grandhorizon.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("bookingCount", bookingDAO.countCurrent());
            Object value = request.getSession(false).getAttribute("userId");
            if (value instanceof Long userId) {
                Optional<User> admin = userDAO.findById(userId);
                admin.ifPresent(user -> request.setAttribute("admin", user));
            }
            request.getRequestDispatcher("/admin-dashboard.jsp").forward(request, response);
        } catch (IOException e) {
            throw new ServletException("Unable to load the admin dashboard.", e);
        }
    }
}
