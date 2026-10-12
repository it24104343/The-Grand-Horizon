package com.grandhorizon.servlet;

import com.grandhorizon.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/profile/delete")
public class DeleteAccountServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long userId = (Long) request.getSession(false).getAttribute("userId");
        String password = request.getParameter("password");
        try {
            if (password == null || password.isBlank()) {
                request.setAttribute("error", "Enter your password to confirm account deletion.");
                request.setAttribute("user", userDAO.findById(userId).orElseThrow());
                request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
                return;
            }
            if (!userDAO.deleteAccount(userId, password)) {
                request.setAttribute("error", "Password did not match. Your account was not deleted.");
                request.setAttribute("user", userDAO.findById(userId).orElseThrow());
                request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
                return;
            }
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/rooms?accountDeleted=true");
        } catch (IOException e) {
            throw new ServletException("Unable to delete user account.", e);
        }
    }
}
