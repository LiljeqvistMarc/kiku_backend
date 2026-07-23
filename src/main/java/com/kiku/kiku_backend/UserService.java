package com.kiku.kiku_backend;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public User findOrCreateUser(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    return userRepository.save(newUser);
                });
    }

    public String generateMagicToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        user.setMagicToken(token);
        user.setMagicTokenExpiry(LocalDateTime.now(java.time.ZoneId.of("UTC")).plusMinutes(15));
        userRepository.save(user);

        return token;
    }

    public User validateMagicToken(String token) {
        User user = userRepository.findByMagicToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (user.getMagicTokenExpiry().isBefore(LocalDateTime.now(java.time.ZoneId.of("UTC")))) {
            throw new RuntimeException("Token expired");
        }

        user.setMagicToken(null);
        user.setMagicTokenExpiry(null);
        userRepository.save(user);

        return user;
    }

    public void generateOtp(String email) {
        User user = findOrCreateUser(email);

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now(java.time.ZoneId.of("UTC")).plusMinutes(15));
        userRepository.save(user);

        try {
            emailService.sendOtpEmail(email, otp);
        } catch (Exception e) {
            System.out.println("Failed to send OTP email: " + e.getMessage());
        }
        System.out.println("OTP for " + email + ": " + otp);
    }

    public User validateOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now(java.time.ZoneId.of("UTC")))) {
            throw new RuntimeException("OTP expired");
        }

        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return user;
    }
}