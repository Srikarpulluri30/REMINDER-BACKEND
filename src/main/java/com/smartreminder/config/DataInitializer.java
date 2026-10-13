package com.smartreminder.config;

import com.smartreminder.entity.Reminder;
import com.smartreminder.entity.User;
import com.smartreminder.repository.ReminderRepository;
import com.smartreminder.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

/**
 * Data Initializer
 * 
 * Automatically ensures verified test user accounts and realistic starter reminders
 * exist upon startup for individual user evaluation:
 * - user@example.com / password123
 * - john.doe@example.com / password123
 */
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDefaultUsers(UserRepository userRepository,
                                              ReminderRepository reminderRepository,
                                              PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultHashedPassword = passwordEncoder.encode("password123");

            // 1. Initialize / Refresh user@example.com (Individual Demo Account)
            User user1 = userRepository.findByEmail("user@example.com")
                    .orElseGet(() -> {
                        User u = new User();
                        u.setName("Alex Smith");
                        u.setEmail("user@example.com");
                        u.setRole("ROLE_USER");
                        return u;
                    });
            user1.setPassword(defaultHashedPassword);
            user1 = userRepository.save(user1);

            // 2. Initialize / Refresh john.doe@example.com
            User user2 = userRepository.findByEmail("john.doe@example.com")
                    .orElseGet(() -> {
                        User u = new User();
                        u.setName("John Doe");
                        u.setEmail("john.doe@example.com");
                        u.setRole("ROLE_USER");
                        return u;
                    });
            user2.setPassword(defaultHashedPassword);
            user2 = userRepository.save(user2);

            // 3. Seed starter sample reminders for user@example.com if empty
            if (reminderRepository.findByUserId(user1.getId()).isEmpty()) {
                createSampleReminder(reminderRepository, user1, "International Passport Renewal",
                        "Passport renewal required for upcoming travel. Apply via online portal.",
                        "PASSPORT", LocalDate.now().plusMonths(6), 30, "UPCOMING");

                createSampleReminder(reminderRepository, user1, "Comprehensive Vehicle Insurance",
                        "Policy #AX-98213 policy renewal with roadside assistance add-on.",
                        "INSURANCE", LocalDate.now().plusDays(10), 14, "DUE_SOON");

                createSampleReminder(reminderRepository, user1, "Driving Licence Card Expiry",
                        "Biometric smart-card driving licence renewal at local transport office.",
                        "LICENCE", LocalDate.now().minusDays(3), 15, "EXPIRED");

                createSampleReminder(reminderRepository, user1, "Cloud Developer Pro Subscription",
                        "Annual cloud computing and certification practice lab subscription.",
                        "SUBSCRIPTION", LocalDate.now().plusMonths(2), 7, "UPCOMING");
            }

            System.out.println("==================================================");
            System.out.println(">> Verified Ready-to-Use User Accounts:");
            System.out.println(">> 1. user@example.com     / password123 (Alex Smith)");
            System.out.println(">> 2. john.doe@example.com / password123 (John Doe)");
            System.out.println("==================================================");
        };
    }

    private void createSampleReminder(ReminderRepository repository, User user, String title,
                                      String description, String category, LocalDate expiryDate,
                                      int reminderDays, String status) {
        Reminder reminder = new Reminder();
        reminder.setTitle(title);
        reminder.setDescription(description);
        reminder.setCategory(category);
        reminder.setExpiryDate(expiryDate);
        reminder.setReminderDays(reminderDays);
        reminder.setReminderDate(expiryDate.minusDays(reminderDays));
        reminder.setStatus(status);
        reminder.setUser(user);
        repository.save(reminder);
    }
}
