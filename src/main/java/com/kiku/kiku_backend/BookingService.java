package com.kiku.kiku_backend;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final UserService userService;
    private final AvailabilityService availabilityService;
    private final GoogleCalendarService googleCalendarService;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public BookingService(
            BookingRepository bookingRepository,
            EmailService emailService,
            UserService userService,
            AvailabilityService availabilityService,
            GoogleCalendarService googleCalendarService) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
        this.userService = userService;
        this.availabilityService = availabilityService;
        this.googleCalendarService = googleCalendarService;
    }

    public Booking createBooking(Booking booking) {
    Availability availability = availabilityService
            .getSlot(booking.getDate(), booking.getTime(), booking.getSessionType())
            .orElseThrow(() -> new RuntimeException("Slot not found"));

    if (availability.isBooked()) {
        throw new RuntimeException("This slot is already booked");
    }

    availability.setBooked(true);
    availabilityService.save(availability); // or availabilityRepository.save directly

    Booking saved = bookingRepository.save(booking);

    User user = userService.findOrCreateUser(saved.getEmail());
    saved.setUser(user);
    bookingRepository.save(saved);
        if ("intro".equals(saved.getSessionType())) {
            saved.setStatus("CONFIRMED");
            try {
                String meetLink = googleCalendarService.createMeetingEvent(
                        saved.getEmail(),
                        saved.getName(),
                        saved.getDate(),
                        saved.getTime(),
                        saved.getSessionType()
                );
                saved.setMeetLink(meetLink);
            } catch (Exception e) {
                System.out.println("Failed to create calendar event: " + e.getMessage());
            }
            bookingRepository.save(saved);
            try {
                emailService.sendBookingConfirmation(
                        saved.getEmail(),
                        saved.getName(),
                        saved.getDate().toString(),
                        saved.getTime().toString(),
                        saved.getSessionType(),
                        saved.getMeetLink()
                );
                emailService.sendBookingNotification(
                        saved.getName(),
                        saved.getEmail(),
                        saved.getDate().toString(),
                        saved.getTime().toString(),
                        saved.getDescription(),
                        saved.getSessionType()
                );
            } catch (Exception e) {
                System.out.println("Failed to send intro booking emails: " + e.getMessage());
            }
        }

        return saved;
    }

    public void cancelBooking(UUID id) throws StripeException {
        Booking booking = getBooking(id);

        LocalDateTime sessionDateTime = LocalDateTime.of(booking.getDate(), booking.getTime());
        LocalDateTime now = LocalDateTime.now(java.time.ZoneId.of("Asia/Tokyo"));
        long hoursUntilSession = Duration.between(now, sessionDateTime).toHours();

        if ("PAID".equals(booking.getStatus())
                && booking.getStripePaymentIntentId() != null
                && hoursUntilSession > 24) {
            Stripe.apiKey = stripeApiKey;
            Refund.create(RefundCreateParams.builder()
                    .setPaymentIntent(booking.getStripePaymentIntentId())
                    .build());
        }

        try {
            availabilityService.markAsUnBooked(booking.getDate(), booking.getTime(), booking.getSessionType());
        } catch (Exception e) {
            System.out.println("Could not unbook slot: " + e.getMessage());
        }

        try {
            emailService.sendCancellationEmail(
                    booking.getEmail(),
                    booking.getName(),
                    booking.getDate().toString(),
                    booking.getTime().toString(),
                    booking.getSessionType()
            );
        } catch (Exception e) {
            System.out.println("Failed to send cancellation email: " + e.getMessage());
        }

        bookingRepository.deleteById(id);
    }

    public Booking getBooking(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }
}