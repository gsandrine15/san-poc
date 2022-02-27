package com.trss.bi.service;

import com.google.common.collect.Sets;
import com.trss.bi.domain.Authority;
import com.trss.bi.domain.Customer;
import com.trss.bi.domain.Role;
import com.trss.bi.domain.User;
import com.trss.bi.repository.AuthorityRepository;
import com.trss.bi.config.Constants;
import com.trss.bi.repository.RoleRepository;
import com.trss.bi.repository.UserApplicationRepository;
import com.trss.bi.repository.UserRepository;
import com.trss.bi.security.SecurityUtils;
import com.trss.bi.service.mapper.RoleMapper;
import com.trss.bi.service.util.PasswordUtil;
import com.trss.bi.service.util.RandomUtil;
import com.trss.bi.service.dto.UserDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.trss.bi.web.rest.errors.InvalidPasswordException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {
    private static final int PASSWORD_RESET_KEY_EXPIRATION_SECS = 86400; // 24 hours

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;

    private final UserApplicationRepository userApplicationRepository;

    private final PasswordHistoryService passwordHistoryService;

    private final CacheManager cacheManager;

    private final RoleMapper roleMapper = new RoleMapper();

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthorityRepository authorityRepository,
                       UserApplicationRepository userApplicationRepository,
                       PasswordHistoryService passwordHistoryService,
                       CacheManager cacheManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.userApplicationRepository = userApplicationRepository;
        this.passwordHistoryService = passwordHistoryService;
        this.cacheManager = cacheManager;
    }

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);
                this.clearUserCaches(user);
                log.debug("Activated user: {}", user);
                return user;
            });
    }

    /**
     * if a password reset key is found and has not expired, return false. else return true
     * @param key
     * @return
     */
    public boolean isPasswordResetKeyExpired(String key) {
        Optional<User> maybeUser = userRepository.findOneByResetKey(key);
        return ! (maybeUser.isPresent() && maybeUser.get().getResetDate().isAfter(Instant.now().minusSeconds(PASSWORD_RESET_KEY_EXPIRATION_SECS)));
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
       log.debug("Reset user password for reset key {}", key);

       PasswordUtil.validatePassword(newPassword);

       String passwordHash = passwordEncoder.encode(newPassword);

       Optional<User> maybeUser = userRepository.findOneByResetKey(key)
           .filter(user -> user.getResetDate().isAfter(Instant.now().minusSeconds(PASSWORD_RESET_KEY_EXPIRATION_SECS)))
           .map(user -> {
                // check the new password against the password history
                passwordHistoryService.checkPasswordHistory(user.getId(), newPassword);

                user.setPassword(passwordHash);
                user.setResetKey(null);
                user.setResetDate(null);
                this.clearUserCaches(user);
                return user;
           });

       if (maybeUser.isPresent()) {
           passwordHistoryService.updatePasswordHistory(maybeUser.get().getId(), newPassword);
       }

       return maybeUser;
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmailIgnoreCase(mail)
            .filter(User::getActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                this.clearUserCaches(user);
                return user;
            });
    }

    public Optional<User> requestPasswordResetByUsername(String username) {
        return userRepository.findOneByLogin(username)
            .filter(User::getActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                this.clearUserCaches(user);
                return user;
            });
    }

    public User registerUser(UserDTO userDTO, String password) {
        PasswordUtil.validatePassword(password);

        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin());
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        newUser.setEmail(userDTO.getEmail());
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        userRepository.save(newUser);
        this.clearUserCaches(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public User createUser(UserDTO userDTO, Customer customer) {
        User user = new User();
        user.setLogin(userDTO.getLogin());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        user.setRole(roleRepository.findByCustomerAndName(customer, userDTO.getRole()));

        String encryptedPassword = passwordEncoder.encode(userDTO.getLogin());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        userRepository.save(user);
        this.clearUserCaches(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user
     * @param lastName last name of user
     * @param email email id of user
     * @param langKey language key
     * @param imageUrl image URL of user
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setEmail(email);
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                this.clearUserCaches(user);
                log.debug("Changed Information for User: {}", user);
            });
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update
     * @return updated user
     */
    public Optional<UserDTO> updateUser(UserDTO userDTO, Customer customer) {
        return Optional.of(userRepository
            .findById(userDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(user -> {
                this.clearUserCaches(user);
                user.setLogin(userDTO.getLogin());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setEmail(userDTO.getEmail());
                user.setImageUrl(userDTO.getImageUrl());
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                user.setRole(roleRepository.findByCustomerAndName(customer, userDTO.getRole()));
                Set<Authority> managedAuthorities = Sets.newHashSet();
                userDTO.getAuthorities().stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(managedAuthorities::add);
                this.clearUserCaches(user);
                log.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(UserDTO::new);
    }

    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            userRepository.delete(user);
            this.clearUserCaches(user);
            log.debug("Deleted User: {}", user);
        });
    }

    public void changePassword(String username, String currentClearTextPassword, String newPassword) {
        PasswordUtil.validatePassword(newPassword);
        userRepository.findOneByLogin(username)
            .ifPresent(user -> {
                String currentEncryptedPassword = user.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }

                // check the new password against the password history
                passwordHistoryService.checkPasswordHistory(user.getId(), newPassword);

                String encryptedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encryptedPassword);
                this.clearUserCaches(user);
                log.debug("Changed password for User: {}", user);
            });
    }

    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin().ifPresent(username -> changePassword(username, currentClearTextPassword, newPassword));
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesAndWithRoleByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithRole(Long id) {
        return userRepository.findOneWithRoleById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithRole() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        List<User> users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS));
        for (User user : users) {
            if (passwordHistoryService.hasPasswordHistory(user.getId())) {
                log.debug("Deleting Password history found for user {}", user.getLogin());
                passwordHistoryService.deleteAllPasswordHistory(user.getId());
            }
            if (!CollectionUtils.isEmpty(userApplicationRepository.findAllByUserId(user.getId()))) {
                log.debug("Deleting user applications found for user {}", user.getLogin());
                userApplicationRepository.deleteAllByUserId(user.getId());
            }
            log.debug("Deleting not activated user {}", user.getLogin());
            userRepository.delete(user);
            this.clearUserCaches(user);
        }
    }

    /**
     * @return a list of all the authorities
     */
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evict(user.getLogin());
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail());
    }
}
