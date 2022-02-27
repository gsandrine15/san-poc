package com.trss.bi.service;

import com.trss.bi.domain.PasswordHistory;
import com.trss.bi.repository.PasswordHistoryRepository;
import com.trss.bi.web.rest.errors.InvalidPasswordException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PasswordHistoryService {

    private PasswordHistoryRepository passwordHistoryRepository;
    private PasswordEncoder passwordEncoder;

    public PasswordHistoryService(PasswordHistoryRepository passwordHistoryRepository, PasswordEncoder passwordEncoder) {
        this.passwordHistoryRepository = passwordHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Add an entry to this user's password history. Only store the most recent 10 passwords.
     * @param userId
     * @param newPassword
     */
    public void updatePasswordHistory(Long userId, String newPassword) {
        // add a new entry
        passwordHistoryRepository.save(new PasswordHistory(userId, passwordEncoder.encode(newPassword)));

        // grab the password history and remove an entry if necessary
        List<PasswordHistory> passwordHistories = passwordHistoryRepository.findAllByUserIdOrderByCreatedDateAsc(userId);
        if (passwordHistories.size() > 10) {
            passwordHistoryRepository.delete(passwordHistories.get(0));
        }
    }

    /**
     * Throw an exception if the newPassword was used as one of the previous 10 passwords for this user.
     * @param userId
     * @param newPassword
     */
    public void checkPasswordHistory(Long userId, String newPassword) {
        List<PasswordHistory> matchedPasswordHistories =
            passwordHistoryRepository.findAllByUserIdOrderByCreatedDateAsc(userId)
                .stream()
                .filter(passwordHistory -> passwordEncoder.matches(newPassword, passwordHistory.getPasswordHash()))
                .collect(Collectors.toList());

        if (!matchedPasswordHistories.isEmpty()) {
            throw new InvalidPasswordException("Previous 10 passwords cannot be used!");
        }
    }

    /**
     * Used to determine if a user has any password history
     * @param userId
     * @return true if the user has any recorded password history. Otherwise, false.
     */
    public boolean hasPasswordHistory(Long userId) {
        return !CollectionUtils.isEmpty(passwordHistoryRepository.findAllByUserId(userId));
    }

    /**
     * Deletes a user's password history
     * @param userId
     */
    public void deleteAllPasswordHistory(Long userId) {
        passwordHistoryRepository.deleteAllByUserId(userId);
    }
}
