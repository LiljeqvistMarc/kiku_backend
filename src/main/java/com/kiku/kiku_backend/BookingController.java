package com.kiku.kiku_backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {

private final BookingService bookingService;
private final StripeService stripeService;
private final JwtService jwtService;
private final BookingRepository bookingRepository;
private final AvailabilityService availabilityService;
private final EmailService emailService;

public BookingController(
    BookingService bookingService, 
    StripeService stripeService,
    JwtService jwtService,
    BookingRepository bookingRepository,
    AvailabilityService availabilityService,
    EmailService emailservice) {
    this.bookingService = bookingService;
    this.stripeService = stripeService;
    this.jwtService = jwtService;
    this.bookingRepository = bookingRepository;
    this.availabilityService = availabilityService;
    this.emailService = emailservice;
}

@PostMapping
public ResponseEntity<Booking> createBooking(@Valid @RequestBody Booking booking) {
    Booking saved = bookingService.createBooking(booking);
    return ResponseEntity.ok(saved);
}

@GetMapping("/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable UUID id) {
        Booking booking = bookingService.getBooking(id);
        return ResponseEntity.ok(booking);
    }

@PostMapping("/{id}/checkout")
public ResponseEntity<?> createCheckout(@PathVariable UUID id) {
    try {
        Booking booking = bookingService.getBooking(id);
        String checkoutUrl = stripeService.createCheckoutSession(
            booking.getSessionType(),
            id.toString(),
            booking.getEmail()
        );
        return ResponseEntity.ok(Map.of("url", checkoutUrl));
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
  }
@PutMapping("/{id}")
public ResponseEntity<Booking> updateBooking(@PathVariable UUID id, @RequestBody Booking newBooking) {
    return bookingRepository.findById(id)
        .map(booking -> {
            // unbook old slot
            String oldSlotType = "intro".equals(booking.getSessionType()) ? "standard" : booking.getSessionType();
            try {
                availabilityService.markAsUnBooked(booking.getDate(), booking.getTime(), oldSlotType);
            } catch (Exception e) {
                System.out.println("Could not unbook old slot: " + e.getMessage());
            }

            // update booking fields
            booking.setDate(newBooking.getDate());
            booking.setTime(newBooking.getTime());
            booking.setSessionType(newBooking.getSessionType());

            // book new slot
            String newSlotType = "intro".equals(booking.getSessionType()) ? "standard" : booking.getSessionType();
            try {
                availabilityService.markAsBooked(booking.getDate(), booking.getTime(), newSlotType);
            } catch (Exception e) {
                System.out.println("Could not book new slot: " + e.getMessage());
            }

            Booking saved = bookingRepository.save(booking);

            // send reschedule email
            try {
                emailService.sendRescheduleEmail(
                    saved.getEmail(),
                    saved.getName(),
                    saved.getDate().toString(),
                    saved.getTime().toString(),
                    saved.getSessionType()
                );
            } catch (Exception e) {
                System.out.println("Failed to send reschedule email: " + e.getMessage());
            }

            return ResponseEntity.ok(saved);
        })
        .orElseThrow(() -> new RuntimeException("Booking not found"));
}

@GetMapping("/my-bookings")
public ResponseEntity<List<Booking>> getMyBookings(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    String token = authHeader.substring(7);
    String email = jwtService.extractEmail(token);
    List<Booking> bookings = bookingRepository.findByUserEmail(email);
    return ResponseEntity.ok(bookings);
}

@DeleteMapping("/{id}")
public ResponseEntity<?> deleteBooking(@PathVariable UUID id) {
    try {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok("Booking cancelled");
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
    }
}
}