package com.grandhorizon.servlet;

import com.grandhorizon.dao.BookingDAO;
import com.grandhorizon.dao.UserDAO;
import com.grandhorizon.model.Booking;
import com.grandhorizon.util.CsrfTokenUtil;
import com.grandhorizon.util.EmailService;
import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/booking/cancel")
public class BookingCancellationServlet extends HttpServlet {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfTokenUtil.isValid(request.getSession(false), request.getParameter("csrfToken"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The cancellation form expired. Reload and try again.");
            return;
        }
        Long userId = (Long) request.getSession(false).getAttribute("userId");
        long bookingId;
        try {
            bookingId = Long.parseLong(request.getParameter("bookingId"));
            if (bookingId < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Choose a valid booking.");
            return;
        }

        try {
            Optional<Booking> cancelled = bookingDAO.cancelUpcoming(bookingId, userId);
            if (cancelled.isEmpty()) {
                request.getSession().setAttribute("bookingNotice",
                        "This booking could not be cancelled. Only your upcoming bookings can be cancelled.");
                response.sendRedirect(request.getContextPath() + "/profile");
                return;
            }

            Booking booking = cancelled.get();
            String notice = "Your booking for room " + booking.getRoomNumber() + " has been cancelled.";
            Optional<String> adminEmail = userDAO.findAdminEmail();
            if (emailService.isConfigured() && adminEmail.isPresent()) {
                try {
                    emailService.sendCancellationAlert(adminEmail.get(), booking);
                } catch (MessagingException | IllegalStateException e) {
                    getServletContext().log("Booking " + booking.getId()
                            + " was cancelled, but the admin alert email could not be sent.", e);
                    notice += " The admin email alert could not be sent.";
                }
            } else if (emailService.isConfigured()) {
                getServletContext().log("Booking " + booking.getId()
                        + " was cancelled; the admin account has no email address configured.");
                notice += " The admin email address is not configured.";
            } else {
                getServletContext().log("Booking " + booking.getId()
                        + " was cancelled; configure SMTP and admin email to send cancellation alerts.");
                notice += " Email notifications are not configured.";
            }
            request.getSession().setAttribute("bookingNotice", notice);
            response.sendRedirect(request.getContextPath() + "/profile");
        } catch (IOException e) {
            throw new ServletException("Unable to cancel the booking.", e);
        }
    }
}
