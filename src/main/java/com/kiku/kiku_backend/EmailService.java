package com.kiku.kiku_backend;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    public void sendBookingConfirmation(String toEmail, String name, String date, String time, String sessionType, String meetLink) throws ResendException {
        Resend resend = new Resend(apiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Kiku Support <support@kiku-support.com>")
                .replyTo("kiku.support@gmail.com")
                .to(toEmail)
                .subject("Your Kiku session is confirmed!")
                .html(buildConfirmationEmail(name, date, time, sessionType, meetLink))
                .build();
        resend.emails().send(params);
    }

    private String buildConfirmationEmail(String name, String date, String time, String sessionType, String meetLink) {
        String meetSection = meetLink != null
            ? "<p>Join your session using this Google Meet link:</p><a href=\"" + meetLink + "\" style=\"background-color: #15803d; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px;\">Join session</a>"
            : "<p>You will receive a Google Meet link by email at least 24 hours before your session.</p>";

        return "<div style=\"font-family: sans-serif; max-width: 600px; margin: 0 auto;\">"
            + "<h2>Your booking is confirmed!</h2>"
            + "<p>Hi " + name + ",</p>"
            + "<p>Your <strong>" + sessionType + " session</strong> has been confirmed for <strong>" + date + " at " + time + "</strong>.</p>"
            + meetSection
            + "<p>If you need to cancel or reschedule, please reply to this email or log in to your dashboard at kiku-support.com/booking/login</p>"
            + "<br>"
            + "<p>Looking forward to talking with you,</p>"
            + "<p><strong>Marc @ Kiku</strong></p>"
            + "</div>";
    }

    public void sendMagicLinkEmail(String toEmail, String magicLink) throws ResendException {
        Resend resend = new Resend(apiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Kiku Support <support@kiku-support.com>")
                .replyTo("kiku.support@gmail.com")
                .to(toEmail)
                .subject("Your Kiku login link")
                .html("""
                    <div style="font-family: sans-serif; max-width: 600px; margin: 0 auto;">
                        <h2>Log in to Kiku</h2>
                        <p>Click the link below to log in. The link expires in 15 minutes.</p>
                        <a href="%s" style="background-color: #15803d; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px;">Log in to Kiku</a>
                        <p>If you didn't request this, you can ignore this email.</p>
                    </div>
                    """.formatted(magicLink))
                .build();
        resend.emails().send(params);
    }

    public void sendBookingNotification(String name, String email, String date, String time, String sessionType, String description) throws ResendException {
        Resend resend = new Resend(apiKey);
        String descriptionLine = (description != null && !description.isEmpty())
            ? "<p><strong>Description:</strong> " + description + "</p>"
            : "";
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Kiku Support <support@kiku-support.com>")
                .replyTo("kiku.support@gmail.com")
                .to("kiku.support@gmail.com")
                .subject("New booking: " + name + " — " + date + " at " + time)
                .html("<div style=\"font-family: sans-serif; max-width: 600px; margin: 0 auto;\">"
                    + "<h2>New booking received</h2>"
                    + "<p><strong>Name:</strong> " + name + "</p>"
                    + "<p><strong>Email:</strong> " + email + "</p>"
                    + "<p><strong>Session:</strong> " + sessionType + "</p>"
                    + "<p><strong>Date:</strong> " + date + "</p>"
                    + "<p><strong>Time:</strong> " + time + "</p>"
                    + descriptionLine
                    + "</div>")
                .build();
        resend.emails().send(params);
    }

    public void sendOtpEmail(String toEmail, String otpCode) throws ResendException {
        Resend resend = new Resend(apiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Kiku Support <support@kiku-support.com>")
                .replyTo("kiku.support@gmail.com")
                .to(toEmail)
                .subject("Your Kiku login code")
                .html("""
                    <div style="font-family: sans-serif; max-width: 600px; margin: 0 auto;">
                        <h2>Your login code</h2>
                        <p>Enter this code to log in to Kiku:</p>
                        <div style="font-size: 36px; font-weight: bold; letter-spacing: 8px; text-align: center; padding: 24px; background: #f0fdf4; border-radius: 8px;">
                            %s
                        </div>
                        <p>This code expires in 15 minutes.</p>
                        <p>If you didn't request this, you can ignore this email.</p>
                    </div>
                    """.formatted(otpCode))
                .build();
        resend.emails().send(params);
    }

    public void sendCancellationEmail(String toEmail, String name, String date, String time, String sessionType) throws ResendException {
        Resend resend = new Resend(apiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Kiku Support <support@kiku-support.com>")
                .replyTo("kiku.support@gmail.com")
                .to(toEmail)
                .subject("Your Kiku booking has been cancelled")
                .html("""
                    <div style="font-family: sans-serif; max-width: 600px; margin: 0 auto;">
                        <h2>Booking cancelled</h2>
                        <p>Hi %s,</p>
                        <p>Your <strong>%s session</strong> on <strong>%s at %s</strong> has been cancelled.</p>
                        <p>If you are eligible for a refund it will appear on your card within a few business days.</p>
                        <p>If you have any questions, please reply to this email.</p>
                        <br>
                        <p><strong>Marc @ Kiku</strong></p>
                    </div>
                    """.formatted(name, sessionType, date, time))
                .build();
        resend.emails().send(params);
    }

    public void sendRescheduleEmail(String toEmail, String name, String newDate, String newTime, String sessionType) throws ResendException {
        Resend resend = new Resend(apiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Kiku Support <support@kiku-support.com>")
                .replyTo("kiku.support@gmail.com")
                .to(toEmail)
                .subject("Your Kiku booking has been rescheduled")
                .html("""
                    <div style="font-family: sans-serif; max-width: 600px; margin: 0 auto;">
                        <h2>Booking rescheduled</h2>
                        <p>Hi %s,</p>
                        <p>Your <strong>%s session</strong> has been rescheduled to <strong>%s at %s</strong>.</p>
                        <p>If you need to make any further changes, please log in to your dashboard at kiku-support.com/booking/login</p>
                        <br>
                        <p><strong>Marc @ Kiku</strong></p>
                    </div>
                    """.formatted(name, sessionType, newDate, newTime))
                .build();
        resend.emails().send(params);
    }
}