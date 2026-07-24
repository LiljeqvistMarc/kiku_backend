package com.kiku.kiku_backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalTime;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {
    List<Availability> findByDate(LocalDate date);
    List<Availability> findByDateAndSessionType(LocalDate date, String sessionType);
    Optional<Availability> findByDateAndTimeAndSessionType(LocalDate date, LocalTime time, String sessionType);
}