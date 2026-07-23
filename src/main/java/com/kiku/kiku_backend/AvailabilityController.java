package com.kiku.kiku_backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

private final AvailabilityService availabilityService;

public AvailabilityController(AvailabilityService availabilityService) {
    this.availabilityService = availabilityService;
}

@PostMapping
public ResponseEntity<Availability> addSlot(@Valid @RequestBody Availability availability) {
    Availability saved = availabilityService.addSlot(availability);
    return ResponseEntity.ok(saved);
}

@GetMapping
    public ResponseEntity<List<Availability>> getSlots(@RequestParam LocalDate date) {
        List<Availability> slots = availabilityService.getSlotsByDate(date);
        return ResponseEntity.ok(slots);
}

@GetMapping("/all")
public ResponseEntity<List<Availability>> getAllSlots() {
    return ResponseEntity.ok(availabilityService.getAllSlots());
}

@DeleteMapping("/{id}")
public ResponseEntity<String> deleteSlot(@PathVariable UUID id) {
    availabilityService.deleteSlot(id);
    return ResponseEntity.ok("Slot deleted");
}

@PostMapping("/bulk")
public ResponseEntity<List<Availability>> addBulkSlots(
        @RequestBody BulkAvailabilityRequest request) {
    return ResponseEntity.ok(availabilityService.addBulkSlots(request));
}

}
