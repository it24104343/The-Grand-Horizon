package com.grandhorizon.util;

import com.grandhorizon.model.Booking;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

// EmailUtil sends booking confirmation emails to the guest using the configured Gmail SMTP settings.
public final class EmailUtil {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String GMAIL_ADDRESS = "mahelampramuditha@gmail.com";
    private static final String APP_PASSWORD = "legz yisf eptw gcqo";

    private EmailUtil() {
    }

    public static boolean isConfigured() {
        return !APP_PASSWORD.isBlank() && !"YOUR_APP_PASSWORD".equals(APP_PASSWORD);
    }

    public static void sendBookingConfirmation(Booking booking) throws MessagingException {
        if (!isConfigured()) {
            throw new IllegalStateException("Set the Gmail app password in EmailUtil before sending email.");
        }
        if (booking.getGuestEmail() == null || booking.getGuestEmail().isBlank()) {
            throw new IllegalStateException("The booking does not have a recipient email address.");
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL_ADDRESS, APP_PASSWORD);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(GMAIL_ADDRESS));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(booking.getGuestEmail()));
        message.setSubject("Your booking is complete", "UTF-8");
        message.setText("Your booking is complete. We warmly welcome you to The Grand Horizon.\n\n"
                + "Room: " + booking.getRoomNumber() + "\n"
                + "Date: " + booking.getBookingDate() + "\n"
                + "Start time: " + booking.getStartTimeAmPm() + "\n"
                + "Duration: " + booking.getDurationHours() + " hour(s)\n"
                + (booking.getMealOptions().isEmpty()
                        ? ""
                        : "Selected meals: " + booking.getMealsDisplay() + "\n")
                + "Total: Rs. " + booking.getTotalAmount(), "UTF-8");
        Transport.send(message);
    }
}
