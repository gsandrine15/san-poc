package com.trss.bi.service;

import com.trss.bi.domain.*;
import com.trss.bi.repository.CustomerRepository;
import com.trss.bi.repository.UserApplicationAccessRepository;
import com.trss.bi.repository.UserDetailRepository;
import com.trss.bi.repository.UserWithDetailRepository;
import com.trss.bi.security.AuthorizationConstants;
import com.trss.bi.security.SecurityUtils;
import com.trss.bi.security.UserPasswordExpiredException;
import com.trss.bi.service.dto.UserDTO;
import com.trss.bi.service.mapper.UserMapper;

import com.trss.bi.service.util.PasswordUtil;
import com.trss.bi.web.rest.errors.BadRequestAlertException;
import com.trss.bi.web.rest.errors.CustomerAccessException;
import com.trss.bi.web.rest.errors.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import javax.persistence.criteria.*;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.trss.bi.security.AuthorizationConstants.*;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserWithDetailService {

    private final Logger log = LoggerFactory.getLogger(UserWithDetailService.class);

    private final UserWithDetailRepository userWithDetailRepository;

    private final CustomerRepository customerRepository;

    private final UserService userService;

    private final UserDetailService userDetailsService;

    private final UserDetailRepository userDetailRepository;

    private final UserMapper userMapper;

    private final UserApplicationAccessRepository userApplicationAccessRepository;

    private final ApplicationService applicationService;

    private final PasswordHistoryService passwordHistoryService;

    private final PasswordEncoder passwordEncoder;

    private final CacheManager cacheManager;

    private final String USER_APPLICATIONS = "userApplications";

    public UserWithDetailService(UserWithDetailRepository userWithDetailRepository,
                                 CustomerRepository customerRepository,
                                 UserService userService,
                                 UserDetailService userDetailService,
                                 UserDetailRepository userDetailRepository,
                                 UserMapper userMapper,
                                 UserApplicationAccessRepository userApplicationAccessRepository,
                                 ApplicationService applicationService,
                                 PasswordHistoryService passwordHistoryService,
                                 PasswordEncoder passwordEncoder,
                                 CacheManager cacheManager) {
        this.userWithDetailRepository = userWithDetailRepository;
        this.customerRepository = customerRepository;
        this.userService = userService;
        this.userDetailsService = userDetailService;
        this.userDetailRepository = userDetailRepository;
        this.userMapper = userMapper;
        this.userApplicationAccessRepository = userApplicationAccessRepository;
        this.applicationService = applicationService;
        this.passwordHistoryService = passwordHistoryService;
        this.passwordEncoder = passwordEncoder;
        this.cacheManager = cacheManager;
    }

    /**
     * Get one UserWithDetail by id.
     *
     * @param id the id of the entity
     * @return optional UserWithDetail
     */
    @Transactional(readOnly = true)
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public Optional<UserWithDetail> findOne(Long id) {
        log.debug("Request to get Customer : {}", id);

        // Check user can view this record
        Optional<UserWithDetail> maybeUserWithDetail = userWithDetailRepository.findById(id);
        if (maybeUserWithDetail.isPresent()) {
            UserWithDetail userWithDetail = maybeUserWithDetail.get();
            // Must be appropriate Admin / Customer Admin or the User Themselves
            if (!SecurityUtils.canAdminCustomer(userWithDetail.getCustomer().getId()) && !SecurityUtils.getCurrentUserId().equals(id)) {
                throw new CustomerAccessException();
            }
        }

        return maybeUserWithDetail;
    }

    /**
     * Create UserWithDetail.
     *
     * @param userWithDetail the entity to save
     * @return the persisted entity
     */
    @PreAuthorize(ACCESS_ADMIN)
    public UserWithDetail createUser(UserWithDetail userWithDetail) {

        // Customer Logic is checked by UserDetailService

        escalatingPrivilegesCheck(userWithDetail.getAuthorities());

        // Create User First
        UserDTO userDTO = new UserDTO();
        userDTO = userWithDetail.updateUserDTO(userDTO);
        User user = userService.createUser(userDTO, userWithDetail.getCustomer());

        // Copy User Id
        userWithDetail.setId(user.getId());

        // Create User Details
        UserDetail userDetail = new UserDetail();
        userDetail = userWithDetail.updateUserDetail(userDetail);
        userDetailsService.save(userDetail);

        // Make sure to clear caches
        clearUserWithDetailCaches(userWithDetail);

        return userWithDetail;
    }

    /**
     * Update UserWithDetail.
     *
     * @param userWithDetail the entity to save
     * @return the persisted entity
     */
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public UserWithDetail updateUser(UserWithDetail userWithDetail) {

        // Customer Logic is checked by UserDetailService
        escalatingPrivilegesCheck(userWithDetail.getAuthorities());

        // Create User Details
        Long userId = userWithDetail.getId();
        Optional<UserDetail> maybeUserDetail = userDetailsService.findOne(userId);
        if (maybeUserDetail.isPresent()) {
            UserDetail userDetail = maybeUserDetail.get();
            userDetail = userWithDetail.updateUserDetail(userDetail);
            userDetailsService.save(userDetail);
        }

        // Update user
        Optional<User> maybeUser = userService.getUserWithRole(userId);
        if (maybeUser.isPresent()) {
            UserDTO userDTO = userMapper.userToUserDTO(maybeUser.get());
            userWithDetail.updateUserDTO(userDTO);
            userService.updateUser(userDTO, userWithDetail.getCustomer());
        }

        clearUserWithDetailCaches(userWithDetail);

        // Refresh with latest
        userWithDetail = this.findOne(userId).orElseThrow(() -> new InternalServerErrorException("User could not be found"));

        // Make sure to clear caches
        return userWithDetail;
    }

    public void updateCurrentUser(String firstName, String lastName, String email, String phoneNumber) {
        Optional<UserWithDetail> maybeUserWithDetail = SecurityUtils.getCurrentUserLogin()
            .map(userWithDetailRepository::findOneByLogin)
            .orElseThrow(() -> new InternalServerErrorException("User could not be found"));

        if (!maybeUserWithDetail.isPresent()) {
            throw new InternalServerErrorException("User could not be found");
        }

        UserWithDetail userWithDetail = maybeUserWithDetail.get();
        Long userId = userWithDetail.getId();

        // Update phone number
        Optional<UserDetail> maybeUserDetail = userDetailsService.findOne(userId);
        if (maybeUserDetail.isPresent()) {
            UserDetail userDetail = maybeUserDetail.get();
            userDetail.setPhoneNumber(phoneNumber);
            userDetailRepository.save(userDetail);
        }

        // Update firstName, lastName, email
        Optional<User> maybeUser = userService.getUserWithRole(userId);
        if (maybeUser.isPresent()) {
            UserDTO userDTO = userMapper.userToUserDTO(maybeUser.get());
            userDTO.setFirstName(firstName);
            userDTO.setLastName(lastName);
            userDTO.setEmail(email);
            userService.updateUser(userDTO, userWithDetail.getCustomer());
        }

        // Refresh with latest
        userWithDetail = this.findOne(userId).orElseThrow(() -> new InternalServerErrorException("User could not be found"));
        clearUserWithDetailCaches(userWithDetail);
    }

    public Long countUsersByRoleId(Long roleId) {
        return userWithDetailRepository.countUsersByRoleIdAndDeletedFalse(roleId);
    }

    /**
     * Check if non-admin is trying to grant admin privileges
     *
     * @param authorities to check
     */
    private void escalatingPrivilegesCheck(Set<Authority> authorities) {
        Role adminRole = new Role(AuthorizationConstants.ADMIN);
        boolean assigningAdminRole = authorities.stream().anyMatch(a -> a.getName().equals(adminRole.getAuthority()));
        if (assigningAdminRole && !(SecurityUtils.isCurrentUserInRole(AuthorizationConstants.ADMIN))) {
            throw new BadRequestAlertException("Only admins can grant admin privileges", "userManagement", "adminAttempt");
        }
    }

    /**
     * Get All Managed Users
     *
     * @param pageable
     * @return list of users with details
     */
    @Transactional(readOnly = true)
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public Page<UserWithDetail> getAllManagedUsers(Pageable pageable) {
        if (SecurityUtils.isCurrentUserInRole(AuthorizationConstants.ADMIN)) {
            return userWithDetailRepository.findAllWithRoleByDeletedFalse(pageable);
        }
        else { // TODO this is never hit as we preAuthoritze with ACCESS_ADMIN_CONSOLE??
            Long customerId = SecurityUtils.getCurrentCustomerId();
            Optional<Customer> customer = customerRepository.findById(customerId);
            if (customer.isPresent()) {
                return userWithDetailRepository.findAllWithRoleByCustomerAndDeletedFalse(pageable, customer.get());
            }
            else {
                return null;
            }
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public Page<UserWithDetail> findAllByPagingCriteria(Pageable pageable, Map<String, String> facets) {
        log.debug("Request to get users by paging criteria, '{}'", facets);

        // if this user is not an admin, then they can only search on users for their customer
        // add or overwrite the customer facet
        if (!(SecurityUtils.isCurrentUserInRole(AuthorizationConstants.ADMIN))) {
            Long customerId = SecurityUtils.getCurrentCustomerId();
            log.debug("User is not an admin, only search across users for this user's customer", customerId);
            facets.put("customer", String.valueOf(customerId));
        }

        Page<UserWithDetail> page = userWithDetailRepository.findAll(new Specification<UserWithDetail>() {
            @Override
            public Predicate toPredicate(Root<UserWithDetail> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();

                // Never return deleted users
                predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("deleted"), Boolean.valueOf(false))));

                if (facets.containsKey("login")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.like(root.get("login"), "%" + facets.get("login") + "%")));
                }

                if (facets.containsKey("firstName")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.like(root.get("firstName"), "%" + facets.get("firstName") + "%")));
                }

                if (facets.containsKey("lastName")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.like(root.get("lastName"), "%" + facets.get("lastName") + "%")));
                }

                if (facets.containsKey("email")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.like(root.get("email"), "%" + facets.get("email") + "%")));
                }

                if (facets.containsKey("phoneNumber")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.like(root.get("phoneNumber"), "%" + facets.get("phoneNumber") + "%")));
                }

                if (facets.containsKey("role")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("role").get("name"), facets.get("role"))));
                }

                if (facets.containsKey("customer")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("customer").get("id"), facets.get("customer"))));
                }

                if (facets.containsKey("activated")) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("activated"), Boolean.valueOf(facets.get("activated")))));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);

        return page;
    }

    /**
     * Get User By Login
     *
     * @param login user login
     * @return optional UserWithDetail
     */
    @Transactional(readOnly = true)
    public Optional<UserWithDetail> getUserByLogin(String login) {

        // Check user can view this record
        Optional<UserWithDetail> maybeUserWithDetail = userWithDetailRepository.findOneWithRoleByLogin(login);
        if (maybeUserWithDetail.isPresent()) {
            UserWithDetail userWithDetail = maybeUserWithDetail.get();
            // Must be appropriate Admin / Customer Admin or the User Themselves
            if (!SecurityUtils.canAdminCustomer(userWithDetail.getCustomer().getId()) && !SecurityUtils.getCurrentUserId().equals(userWithDetail.getId())) {
                throw new CustomerAccessException();
            }
            // If deleted set result to empty
            if (userWithDetail.getDeleted()) {
                maybeUserWithDetail = Optional.empty();
            }
        }

        return maybeUserWithDetail;
    }

    /**
     * Get User By Login and skip authorization
     * Only use this during the login process
     *
     * @param login user login
     * @return optional UserWithDetail
     */
    @Transactional(readOnly = true)
    public Optional<UserWithDetail> getUserByLoginNoAuth(String login) {
        return userWithDetailRepository.findOneWithRoleByLogin(login);
    }

    /**
     * Delete User By Login
     *
     * @param login user login
     */
    @PreAuthorize(ACCESS_ADMIN)
    public void deleteUserByLogin(String login) {
        Optional<UserWithDetail> maybeUserWithDetail = getUserByLogin(login);
        if ( maybeUserWithDetail.isPresent() ) {
            UserWithDetail userWithDetail = maybeUserWithDetail.get();
            userDetailsService.delete(userWithDetail.getId());

            // Clear cache
            clearUserWithDetailCaches(userWithDetail);
        }
    }

    /**
     * Get Application Access for User
     *
     * @param userId
     * @return List<UserApplicationAccess>
     */
    @Cacheable(cacheNames = USER_APPLICATIONS)
    public List<UserApplicationAccess> getUserApplicationAccess(Long userId) {
        log.info("getUserApplicationAccess");
        Optional<UserWithDetail> maybeUserWithDetail = findOne(userId);
        if (!maybeUserWithDetail.isPresent() || maybeUserWithDetail.get().getDeleted()) {
            throw new BadRequestAlertException("Invalid id", USER_APPLICATIONS, "idnull");
        }
        UserWithDetail userWithDetail = maybeUserWithDetail.get();
        List<UserApplicationAccess> accessList = userApplicationAccessRepository.findAllByUserWithDetailId(userWithDetail.getId());
        addSystemAssignedApplications(userWithDetail, accessList);
        return accessList.stream().filter(a -> !a.getStatus().equals(ApplicationAssignmentStatus.HIDDEN)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean roleInUse(Long roleId) {
        return userWithDetailRepository.existsUserWithDetailByRoleId(roleId);
    }

    /**
     * Get user applications with the specified status
     *
     * @param userWithDetail user to get applications for
     * @param status desired application status
     * @return List<UserApplicationAccess>
     */
    public List<UserApplicationAccess> getUserApplicationsByStatus(UserWithDetail userWithDetail, ApplicationAssignmentStatus status) {
        List<UserApplicationAccess> accessList = userApplicationAccessRepository.findAllByUserWithDetailId(userWithDetail.getId());
        addSystemAssignedApplications(userWithDetail, accessList);
        return accessList.stream().filter(a -> a.getStatus().equals(status)).collect(Collectors.toList());
    }

    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin().ifPresent(username -> changePasswordNoAuth(username, currentClearTextPassword, newPassword));
    }

    public void changePasswordNoAuth(String username, String currentClearTextPassword, String newPassword) {
        // Attempt to change password
        PasswordUtil.validatePassword(newPassword);
        userService.changePassword(username, currentClearTextPassword, newPassword);
        updatePasswordHistory(username, newPassword);
    }

    public void updatePasswordHistory(String username, String newPassword) {
        Optional<UserWithDetail> maybeUser = getUserByLoginNoAuth(username);

        // Update password date if successful
        maybeUser.ifPresent(userWithDetail -> {
            userDetailsService.findOneNoAuth(userWithDetail.getId())
                .ifPresent(userDetail -> {
                    userDetail.setPasswordDate(Instant.now());
                    userDetailRepository.save(userDetail);
                });
            // update the password history
            passwordHistoryService.updatePasswordHistory(userWithDetail.getId(), newPassword);
        });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        Optional<User> maybeUser = userService.completePasswordReset(newPassword, key);

        maybeUser.ifPresent(userWithDetail -> {
            userDetailsService.findOneNoAuth(userWithDetail.getId())
                .ifPresent(userDetail -> {
                    userDetail.setPasswordDate(Instant.now());
                    userDetailRepository.save(userDetail);
                });
        });

        return maybeUser;
    }

    public boolean isLoginCredentialsCorrect(String username, String password) {
        Optional<UserWithDetail> maybeUserWithDetail = userWithDetailRepository.findOneByLogin(username);
        if (maybeUserWithDetail.isPresent()) {
            return passwordEncoder.matches(password, maybeUserWithDetail.get().getPassword());
        }
        return false;
    }

    public boolean checkUserPasswordExpired(String username) {
        Optional<UserWithDetail> maybeUserWithDetail = getUserByLoginNoAuth(username);
        if (maybeUserWithDetail.isPresent()) {
            UserWithDetail userWithDetai1 = maybeUserWithDetail.get();
            Customer customer = userWithDetai1.getCustomer();

            try {
                checkPasswordExpired(userWithDetai1.getPasswordDate(), customer.getPasswordExpiration());
            } catch (UserPasswordExpiredException e) {
                return true;
            }
        }

        return false;
    }

    public static void checkPasswordExpired(Instant passwordDate, Integer passwordExpirationDays) {
        Long daysSincePasswordCreated = Duration.between(passwordDate, Instant.now()).toDays();
        if (daysSincePasswordCreated >= passwordExpirationDays) {
            throw new UserPasswordExpiredException("Password has expired!");
        }
    }

    /**
     * Adds systems assigned applications for user
     *
     * @param userWithDetail user to check for system assigned applications
     * @param userApplicationAccess list of applications that might be updated
     */
    private void addSystemAssignedApplications(UserWithDetail userWithDetail, List<UserApplicationAccess> userApplicationAccess) {
        if (ADMIN.equals(userWithDetail.getRole().getName()) || CUSTOMER_ADMIN.equals(userWithDetail.getRole().getName())) {
            Optional<Application> console = applicationService.findOneNoAuth(Application.CONSOLE);
            if (console.isPresent()) {
                userApplicationAccess.add(new UserApplicationAccess(userWithDetail, console.get(), ApplicationAssignmentStatus.AVAILABLE));
            }
        }
    }

    /**
     * Clear appropriates UserWithDetail caches
     *
     * @param userWithDetail that has changed
     */
    private void clearUserWithDetailCaches(UserWithDetail userWithDetail) {
        Objects.requireNonNull(cacheManager.getCache(UserWithDetailRepository.USER_WITH_DETAIL_BY_ID)).evict(userWithDetail.getId());
        Objects.requireNonNull(cacheManager.getCache(UserWithDetailRepository.USER_WITH_DETAIL_BY_LOGIN)).evict(userWithDetail.getLogin());
        Objects.requireNonNull(cacheManager.getCache(USER_APPLICATIONS)).evict(userWithDetail.getId());
    }
}
