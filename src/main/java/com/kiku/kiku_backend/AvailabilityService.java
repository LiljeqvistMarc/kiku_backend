package com.kiku.kiku_backend;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository){
        this.availabilityRepository = availabilityRepository;
    }
    
    public List<Availability> getSlotsByDate(LocalDate date){
         return availabilityRepository.findByDateAndBookedFalse(date);
    }

    public Availability addSlot(Availability availability) {
        return availabilityRepository.save(availability);
    }

    public Optional<Availability> getSlot(LocalDate date, LocalTime time, String sessionType) {
        return availabilityRepository.findByDateAndTimeAndSessionType(date, time, sessionType);
    
    }
    public Availability save(Availability availability) {
          return availabilityRepository.save(availability);
    }

    public void markAsBooked(LocalDate date, LocalTime time, String sessionType) {
        Availability availability = availabilityRepository.findByDateAndTimeAndSessionType(date, time, sessionType)
        .orElseThrow(() -> new RuntimeException("Data not found"));
        availability.setBooked(true);

        availabilityRepository.save(availability);
    }

    public void markAsUnBooked(LocalDate date, LocalTime time, String sessionType) {
        Availability availability = availabilityRepository.findByDateAndTimeAndSessionType(date, time, sessionType)
        .orElseThrow(() -> new RuntimeException("Data not found"));
        availability.setBooked(false);

        availabilityRepository.save(availability);
    }

    public void deleteSlot(UUID id) {
    availabilityRepository.deleteById(id);
    }

    public List<Availability> getAllSlots() {
    return availabilityRepository.findAll();
}
public List<Availability> addBulkSlots(BulkAvailabilityRequest request) {
    List<Availability> slots = new ArrayList<>();
    LocalDate current = request.getStartDate();

    while (!current.isAfter(request.getEndDate())) {
        String dayName = current.getDayOfWeek().name();
        if (request.getDaysOfWeek().contains(dayName)) {
            for (LocalTime time : request.getTimes()) {
                Availability slot = new Availability();
                slot.setDate(current);
                slot.setTime(time);
                slot.setSessionType(request.getSessionType());
                slot.setBooked(false);
                slots.add(slot);
            }
        }
        current = current.plusDays(1);
    }

    return availabilityRepository.saveAll(slots);
}
}
