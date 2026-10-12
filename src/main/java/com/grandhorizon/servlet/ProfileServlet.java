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

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long userId = (Long) request.getSession(false).getAttribute("userId");
        try {
            Optional<User> user = userDAO.findById(userId);
            if (user.isEmpty()) {
                request.getSession(false).invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            request.setAttribute("user", user.get());
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
        } catch (IOException e) {
            throw new ServletException("Unable to load user profile.", e);
        }
    }
}
