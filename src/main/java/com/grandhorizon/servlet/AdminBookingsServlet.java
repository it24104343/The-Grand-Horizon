package com.grandhorizon.servlet;

import com.grandhorizon.dao.BookingDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/bookings")
// AdminBookingsServlet prepares the admin booking list and cancelled booking list for management screens.
public class AdminBookingsServlet extends HttpServlet {
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("bookings", bookingDAO.findCurrent());
            request.setAttribute("cancelledBookings", bookingDAO.findCancelled());
            request.setAttribute("bookingCount", bookingDAO.countCurrent());
            request.getRequestDispatcher("/WEB-INF/views/admin-bookings.jsp").forward(request, response);
        } catch (IOException e) {
            throw new ServletException("Unable to load booking records.", e);
        }
    }
}
