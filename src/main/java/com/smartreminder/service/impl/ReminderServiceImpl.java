package com.smartreminder.service.impl;

import com.smartreminder.dto.request.ReminderRequest;
import com.smartreminder.dto.response.PagedResponse;
import com.smartreminder.dto.response.ReminderResponse;
import com.smartreminder.entity.Reminder;
import com.smartreminder.entity.User;
import com.smartreminder.exception.ResourceNotFoundException;
import com.smartreminder.repository.ReminderRepository;
import com.smartreminder.repository.UserRepository;
import com.smartreminder.service.AuditNotificationService;
import com.smartreminder.service.ReminderService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reminder Service Implementation
 * 
 * Implements deterministic status calculation, smart date calculation,
 * in-memory caching (@Cacheable / @CacheEvict), and asynchronous audit logging (@Async).
 */
@Service
public class ReminderServiceImpl implements ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final AuditNotificationService auditNotificationService;

    public ReminderServiceImpl(ReminderRepository reminderRepository,
                               UserRepository userRepository,
                               AuditNotificationService auditNotificationService) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        this.auditNotificationService = auditNotificationService;
    }

    @Override
    @Transactional
    @CacheEvict(value = "adminStats", allEntries = true)
    public ReminderResponse createReminder(ReminderRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        // 1. Calculate reminder alert date: expiryDate - reminderDays
        LocalDate reminderDate = request.getExpiryDate().minusDays(request.getReminderDays());

        // 2. Determine initial status based on today's date
        String status = evaluateStatus(request.getExpiryDate(), reminderDate, "UPCOMING");

        // 3. Construct Reminder entity
        Reminder reminder = new Reminder();
        reminder.setTitle(request.getTitle().trim());
        reminder.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        reminder.setCategory(request.getCategory().trim().toUpperCase());
        reminder.setExpiryDate(request.getExpiryDate());
        reminder.setReminderDays(request.getReminderDays());
        reminder.setReminderDate(reminderDate);
        reminder.setStatus(status);
        reminder.setUser(user);

        // 4. Save to MySQL
        Reminder savedReminder = reminderRepository.save(reminder);

        // 5. Asynchronous Audit Notification Dispatch (non-blocking)
        auditNotificationService.logReminderActivityAsync(
                "CREATE_REMINDER",
                savedReminder.getId(),
                userEmail,
                "Title: " + savedReminder.getTitle() + ", Expiry: " + savedReminder.getExpiryDate()
        );

        return mapToResponse(savedReminder);
    }

    @Override
    @Transactional
    public PagedResponse<ReminderResponse> getAllUserRemindersPaged(
            String userEmail,
            String category,
            String status,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        List<ReminderResponse> allFiltered = getAllUserReminders(userEmail, category, status, search);

        Comparator<ReminderResponse> comparator;
        String sortProperty = (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy.trim().toLowerCase() : "expirydate";

        switch (sortProperty) {
            case "title":
                comparator = Comparator.comparing(r -> r.getTitle().toLowerCase());
                break;
            case "reminderdate":
                comparator = Comparator.comparing(ReminderResponse::getReminderDate);
                break;
            case "createdat":
                comparator = Comparator.comparing(ReminderResponse::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "expirydate":
            default:
                comparator = Comparator.comparing(ReminderResponse::getExpiryDate);
                break;
        }

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        List<ReminderResponse> sortedList = allFiltered.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        int totalElements = sortedList.size();
        int safePage = Math.max(0, page);
        int safeSize = (size > 0 && size <= 100) ? size : 10;
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);

        int fromIndex = safePage * safeSize;
        List<ReminderResponse> pageContent;

        if (fromIndex >= totalElements) {
            pageContent = List.of();
        } else {
            int toIndex = Math.min(fromIndex + safeSize, totalElements);
            pageContent = sortedList.subList(fromIndex, toIndex);
        }

        boolean isFirst = safePage == 0;
        boolean isLast = totalPages == 0 || safePage >= totalPages - 1;

        return new PagedResponse<>(
                pageContent,
                safePage,
                safeSize,
                totalElements,
                totalPages,
                isFirst,
                isLast
        );
    }

    @Override
    @Transactional
    public List<ReminderResponse> getAllUserReminders(String userEmail, String category, String status, String search) {
        User user = getUserByEmail(userEmail);
        List<Reminder> reminders = reminderRepository.findByUserId(user.getId());

        List<Reminder> updatedReminders = reminders.stream()
                .map(this::refreshStatusIfNeeded)
                .collect(Collectors.toList());

        return updatedReminders.stream()
                .filter(r -> category == null || category.trim().isEmpty() || r.getCategory().equalsIgnoreCase(category.trim()))
                .filter(r -> status == null || status.trim().isEmpty() || r.getStatus().equalsIgnoreCase(status.trim()))
                .filter(r -> search == null || search.trim().isEmpty() ||
                        r.getTitle().toLowerCase().contains(search.trim().toLowerCase()) ||
                        (r.getDescription() != null && r.getDescription().toLowerCase().contains(search.trim().toLowerCase())))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Cacheable(value = "reminders", key = "#id + '_' + #userEmail")
    public ReminderResponse getReminderById(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);

        Reminder reminder = reminderRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with ID: " + id));

        Reminder refreshed = refreshStatusIfNeeded(reminder);
        return mapToResponse(refreshed);
    }

    @Override
    @Transactional
    @CacheEvict(value = "reminders", key = "#id + '_' + #userEmail")
    public ReminderResponse updateReminder(Long id, ReminderRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        Reminder reminder = reminderRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with ID: " + id));

        LocalDate reminderDate = request.getExpiryDate().minusDays(request.getReminderDays());
        String status = evaluateStatus(request.getExpiryDate(), reminderDate, reminder.getStatus());

        reminder.setTitle(request.getTitle().trim());
        reminder.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        reminder.setCategory(request.getCategory().trim().toUpperCase());
        reminder.setExpiryDate(request.getExpiryDate());
        reminder.setReminderDays(request.getReminderDays());
        reminder.setReminderDate(reminderDate);
        reminder.setStatus(status);

        Reminder updatedReminder = reminderRepository.save(reminder);

        auditNotificationService.logReminderActivityAsync(
                "UPDATE_REMINDER",
                id,
                userEmail,
                "Updated title to: " + updatedReminder.getTitle()
        );

        return mapToResponse(updatedReminder);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"reminders", "adminStats"}, allEntries = true)
    public void deleteReminder(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);

        Reminder reminder = reminderRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with ID: " + id));

        reminderRepository.delete(reminder);

        auditNotificationService.logReminderActivityAsync(
                "DELETE_REMINDER",
                id,
                userEmail,
                "Deleted reminder with ID: " + id
        );
    }

    @Override
    @Transactional
    public List<ReminderResponse> getUpcomingReminders(String userEmail) {
        return getAllUserReminders(userEmail, null, "UPCOMING", null);
    }

    @Override
    @Transactional
    public List<ReminderResponse> getDueSoonReminders(String userEmail) {
        return getAllUserReminders(userEmail, null, "DUE_SOON", null);
    }

    @Override
    @Transactional
    public List<ReminderResponse> getExpiredReminders(String userEmail) {
        return getAllUserReminders(userEmail, null, "EXPIRED", null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "reminders", key = "#id + '_' + #userEmail")
    public ReminderResponse markAsCompleted(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);

        Reminder reminder = reminderRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with ID: " + id));

        reminder.setStatus("COMPLETED");
        Reminder updatedReminder = reminderRepository.save(reminder);

        auditNotificationService.logReminderActivityAsync(
                "COMPLETE_REMINDER",
                id,
                userEmail,
                "Marked reminder as COMPLETED"
        );

        return mapToResponse(updatedReminder);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public String evaluateStatus(LocalDate expiryDate, LocalDate reminderDate, String currentStatus) {
        if ("COMPLETED".equalsIgnoreCase(currentStatus)) {
            return "COMPLETED";
        }

        LocalDate today = LocalDate.now();

        if (today.isAfter(expiryDate)) {
            return "EXPIRED";
        } else if (!today.isBefore(reminderDate) && !today.isAfter(expiryDate)) {
            return "DUE_SOON";
        } else {
            return "UPCOMING";
        }
    }

    private Reminder refreshStatusIfNeeded(Reminder reminder) {
        String dynamicStatus = evaluateStatus(reminder.getExpiryDate(), reminder.getReminderDate(), reminder.getStatus());
        if (!dynamicStatus.equals(reminder.getStatus())) {
            reminder.setStatus(dynamicStatus);
            return reminderRepository.save(reminder);
        }
        return reminder;
    }

    private ReminderResponse mapToResponse(Reminder reminder) {
        return new ReminderResponse(
                reminder.getId(),
                reminder.getTitle(),
                reminder.getDescription(),
                reminder.getCategory(),
                reminder.getExpiryDate(),
                reminder.getReminderDays(),
                reminder.getReminderDate(),
                reminder.getStatus(),
                reminder.getCreatedAt(),
                reminder.getUpdatedAt(),
                reminder.getUser().getId(),
                reminder.getUser().getEmail()
        );
    }
}
