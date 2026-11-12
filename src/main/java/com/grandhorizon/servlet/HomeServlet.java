package com.grandhorizon.servlet;

import com.grandhorizon.dao.BookingDAO;
import com.grandhorizon.dao.RoomDAO;
import com.grandhorizon.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/rooms")
// HomeServlet loads the room listing page and passes the current booking counts to the JSP.
public class HomeServlet extends HttpServlet {
    private final RoomDAO roomDAO = new RoomDAO();
    private final UserDAO userDAO = new UserDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("rooms", roomDAO.findAvailableRooms());
            request.setAttribute("adminEmail", userDAO.findAdminEmail().orElse(""));
            request.setAttribute("bookingCount", bookingDAO.countCurrent());
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        } catch (IOException e) {
            throw new ServletException("Unable to load available rooms.", e);
        }
    }
}
