package com.kiku.kiku_backend;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {

    private final EmailService emailService;
    private final BookingService bookingService;
    private final GoogleCalendarService googleCalendarService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public StripeWebhookController(BookingService bookingService, EmailService emailService, GoogleCalendarService googleCalendarService) {
        this.bookingService = bookingService;
        this.emailService = emailService;
        this.googleCalendarService = googleCalendarService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            try {
                String rawJson = event.getDataObjectDeserializer().getRawJson();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(rawJson);
                String bookingId = root.path("metadata").path("booking_id").asText();

                if (!bookingId.isEmpty()) {
                    Booking booking = bookingService.getBooking(UUID.fromString(bookingId));
                    booking.setStatus("PAID");

                    String paymentIntentId = root.path("payment_intent").asText();
                    if (!paymentIntentId.isEmpty()) {
                        booking.setStripePaymentIntentId(paymentIntentId);
                    }

                    try {
                        String meetLink = googleCalendarService.createMeetingEvent(
                                booking.getEmail(),
                                booking.getName(),
                                booking.getDate(),
                                booking.getTime(),
                                booking.getSessionType()
                        );
                        booking.setMeetLink(meetLink);
                    } catch (Exception e) {
                        System.out.println("Failed to create calendar event: " + e.getMessage());
                    }

                    bookingService.save(booking);

                    emailService.sendBookingConfirmation(
                            root.path("customer_details").path("email").asText(),
                            root.path("customer_details").path("name").asText(),
                            booking.getDate().toString(),
                            booking.getTime().toString(),
                            booking.getSessionType(),
                            booking.getMeetLink());

                    emailService.sendBookingNotification(
                            booking.getName(),
                            booking.getEmail(),
                            booking.getDate().toString(),
                            booking.getTime().toString(),
                            booking.getDescription(),
                            booking.getSessionType());
                }

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                return ResponseEntity.internalServerError().body("Webhook processing failed");
            }
        }

        return ResponseEntity.ok("Received");
    }
}