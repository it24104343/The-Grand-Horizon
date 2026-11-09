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

public class EmailService {
    // Set MAIL_SMTP_HOST, MAIL_SMTP_PORT, MAIL_SMTP_USERNAME, MAIL_SMTP_PASSWORD,
    // MAIL_FROM, MAIL_SMTP_AUTH, and MAIL_SMTP_STARTTLS in the application environment.
    public boolean isConfigured() {
        if (!hasValue("MAIL_SMTP_HOST") || !hasValue("MAIL_FROM")) {
            return false;
        }
        boolean authenticationRequired = Boolean.parseBoolean(settingOrDefault("MAIL_SMTP_AUTH", "true"));
        return !authenticationRequired || (hasValue("MAIL_SMTP_USERNAME") && hasValue("MAIL_SMTP_PASSWORD"));
    }

    public void sendBookingConfirmation(Booking booking) throws MessagingException {
        send(booking.getGuestEmail(), "Your booking is complete",
                "Your booking is complete. We warmly welcome you to The Grand Horizon.\n\n"
                        + "Room: " + booking.getRoomNumber() + "\n"
                        + "Date: " + booking.getBookingDate() + "\n"
                        + "Start time: " + booking.getStartTime() + "\n"
                        + "Duration: " + booking.getDurationHours() + " hour(s)\n"
                        + "Total: Rs. " + booking.getTotalAmount());
    }

    public void sendCancellationAlert(String adminEmail, Booking booking) throws MessagingException {
        send(adminEmail, "Room booking cancelled",
                "A guest cancelled a booking at The Grand Horizon.\n\n"
                        + "Guest: " + booking.getGuestName() + "\n"
                        + "Email: " + booking.getGuestEmail() + "\n"
                        + "Room: " + booking.getRoomNumber() + "\n"
                        + "Date: " + booking.getBookingDate() + "\n"
                        + "Start time: " + booking.getStartTime() + "\n"
                        + "Duration: " + booking.getDurationHours() + " hour(s)");
    }

    private void send(String recipient, String subject, String body) throws MessagingException {
        if (!isConfigured() || recipient == null || recipient.isBlank()) {
            throw new IllegalStateException("Email or recipient is not configured.");
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", setting("MAIL_SMTP_HOST"));
        properties.put("mail.smtp.port", settingOrDefault("MAIL_SMTP_PORT", "587"));
        properties.put("mail.smtp.auth", settingOrDefault("MAIL_SMTP_AUTH", "true"));
        properties.put("mail.smtp.starttls.enable", settingOrDefault("MAIL_SMTP_STARTTLS", "true"));
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        String username = setting("MAIL_SMTP_USERNAME");
        String password = setting("MAIL_SMTP_PASSWORD");
        Session session;
        if (!username.isBlank()) {
            session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        } else {
            session = Session.getInstance(properties);
        }

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(setting("MAIL_FROM")));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        message.setSubject(subject, "UTF-8");
        message.setText(body, "UTF-8");
        Transport.send(message);
    }

    private boolean hasValue(String name) {
        return !setting(name).isBlank();
    }

    private String setting(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value.trim();
    }

    private String settingOrDefault(String name, String defaultValue) {
        String value = setting(name);
        return value.isBlank() ? defaultValue : value;
    }
}
