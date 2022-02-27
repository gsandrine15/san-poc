package com.trss.bi.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.trss.bi.config.Constants;
import com.trss.bi.domain.User;
import com.trss.bi.domain.UserApplicationAccess;
import com.trss.bi.domain.UserWithDetail;
import com.trss.bi.repository.UserRepository;
import com.trss.bi.security.SecurityUtils;
import com.trss.bi.service.CustomerService;
import com.trss.bi.service.MailService;
import com.trss.bi.service.UserService;
import com.trss.bi.service.UserWithDetailService;
import com.trss.bi.service.dto.UserWithDetailDTO;
import com.trss.bi.service.mapper.UserWithDetailMapper;
import com.trss.bi.web.rest.errors.BadRequestAlertException;
import com.trss.bi.web.rest.errors.CustomerAccessException;
import com.trss.bi.web.rest.errors.EmailAlreadyUsedException;
import com.trss.bi.web.rest.errors.LoginAlreadyUsedException;
import com.trss.bi.web.rest.util.HeaderUtil;
import com.trss.bi.web.rest.util.PaginationUtil;
import com.trss.bi.web.rest.util.RequestParamUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.trss.bi.security.AuthorizationConstants.*;

/**
 * REST controller for managing users.
 * <p>
 * This class accesses the User entity, and needs to fetch its collection of authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api")
public class UserResource {

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    private final UserService userService;

    private final CustomerService customerService;

    private final UserRepository userRepository;

    private final MailService mailService;

    private final UserWithDetailService userWithDetailService;

    private final UserWithDetailMapper userWithDetailMapper;

    public UserResource(UserService userService,
                        CustomerService customerService,
                        UserRepository userRepository,
                        MailService mailService,
                        UserWithDetailService userWithDetailService,
                        UserWithDetailMapper userWithDetailMapper) {

        this.userService = userService;
        this.customerService = customerService;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.userWithDetailService = userWithDetailService;
        this.userWithDetailMapper = userWithDetailMapper;
    }

    /**
     * POST  /users  : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends an
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userWithDetailDTO the user to create
     * @return the ResponseEntity with status 201 (Created) and with body the new user, or with status 400 (Bad Request) if the login or email is already in use
     * @throws URISyntaxException if the Location URI syntax is incorrect
     * @throws BadRequestAlertException 400 (Bad Request) if the login or email is already in use
     */
    @PostMapping("/users")
    @Timed
    @PreAuthorize(ACCESS_ADMIN)
    public ResponseEntity<UserWithDetailDTO> createUser(@Valid @RequestBody UserWithDetailDTO userWithDetailDTO) throws URISyntaxException {
        log.debug("REST request to save User : {}", userWithDetailDTO);

        // Map to UserWithDetail
        UserWithDetail userWithDetail = userWithDetailMapper.userDTOToUser(userWithDetailDTO);

        if (userWithDetail.getLogin().contains("@")) {
            throw new BadRequestAlertException("Username cannot contain an @ symbol", "userManagement", "invalidusername");
        }
        else if (userWithDetail.getId() != null) {
            throw new BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists");
            // Lowercase the user login before comparing with database
        } else if (userRepository.findOneByLogin(userWithDetail.getLogin().toLowerCase()).isPresent()) {
            throw new LoginAlreadyUsedException();
        } else if (userWithDetailDTO.getEmail() != null && userRepository.findOneByEmailIgnoreCase(userWithDetail.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException();
        } else if (userWithDetail.getCustomer() == null || userWithDetail.getCustomer().getId() == null) {
            throw new BadRequestAlertException("User must have a customer a customer id", "userManagement", "customeridrequired");
        } else if (!SecurityUtils.canAdminCustomer(userWithDetail.getCustomer().getId())) {
            throw new CustomerAccessException();
        } else {
            UserWithDetail newUserWithDetail = userWithDetailService.createUser(userWithDetail);

            Optional<User> maybeNewUser = userService.getUserWithRole(newUserWithDetail.getId());
            if (maybeNewUser.isPresent()) {
                User newUser = maybeNewUser.get();
                mailService.sendCreationEmail(newUser);
                return ResponseEntity.created(new URI("/api/users/" + newUser.getLogin()))
                    .headers(HeaderUtil.createAlert("userManagement.created", newUser.getLogin()))
                    .body(new UserWithDetailDTO(newUserWithDetail));
            }
            else {
                throw new BadRequestAlertException("Error creating user", "userManagement", "usercreateerror");
            }
        }
    }

    /**
     * PUT /users : Updates an existing User.
     *
     * @param userWithDetailDTO the user to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated user
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already in use
     * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is already in use
     */
    @PutMapping("/users")
    @Timed
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public ResponseEntity<UserWithDetailDTO> updateUser(@Valid @RequestBody UserWithDetailDTO userWithDetailDTO) {
        log.debug("REST request to update User : {}", userWithDetailDTO);

        UserWithDetail userWithDetail = userWithDetailMapper.userDTOToUser(userWithDetailDTO);

        if (userWithDetail.getLogin().contains("@")) {
            throw new BadRequestAlertException("Username cannot contain an @ symbol", "userManagement", "invalidusername");
        }

        Optional<User> existingUser;
        if (userWithDetailDTO.getEmail() != null) {
            existingUser = userRepository.findOneByEmailIgnoreCase(userWithDetailDTO.getEmail());
            if (existingUser.isPresent() && (!existingUser.get().getId().equals(userWithDetail.getId()))) {
                throw new EmailAlreadyUsedException();
            } else if (existingUser.isPresent() && SecurityUtils.isCurrentUserInRole(CUSTOMER_ADMIN)) {
                User user = existingUser.get();
                if (user.getRole().getName().equals(ADMIN) || userWithDetailDTO.getRole().getName().equals(ADMIN)) {
                    throw new IllegalArgumentException("Customer admins cannot modify or assign admin users");
                }
                if (!user.getRole().getName().equals(CUSTOMER_ADMIN) && userWithDetailDTO.getRole().getName().equals(CUSTOMER_ADMIN)) {
                    throw new IllegalArgumentException("Customer admins cannot promote users to customer admin access");
                }
            }
        }

        existingUser = userRepository.findOneByLogin(userWithDetail.getLogin().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(userWithDetail.getId()))) {
            throw new LoginAlreadyUsedException();
        }
        if (userWithDetail.getCustomer() == null || userWithDetail.getCustomer().getId() == null) {
            throw new BadRequestAlertException("User must have a customer a customer id", "userManagement", "customeridrequired");
        } else if (!SecurityUtils.canAdminCustomer(userWithDetail.getCustomer().getId())) {
            throw new CustomerAccessException();
        }
        if (existingUser.isPresent() && SecurityUtils.isCurrentUserInRole(CUSTOMER_ADMIN)) {
            User user = existingUser.get();
            if (user.getRole().getName().equals(ADMIN) || userWithDetailDTO.getRole().getName().equals(ADMIN)) {
                throw new IllegalArgumentException("Customer admins cannot modify or assign admin users");
            }
            if (!user.getRole().getName().equals(CUSTOMER_ADMIN) && userWithDetailDTO.getRole().getName().equals(CUSTOMER_ADMIN)) {
                throw new IllegalArgumentException("Customer admins cannot promote users to customer admin access");
            }
        }

        UserWithDetail updatedUserWithDetail = userWithDetailService.updateUser(userWithDetail);
        Optional<UserWithDetailDTO> maybeUserWithDetailDTO = Optional.of(new UserWithDetailDTO(updatedUserWithDetail));

        return ResponseUtil.wrapOrNotFound(maybeUserWithDetailDTO,
            HeaderUtil.createAlert("userManagement.updated", updatedUserWithDetail.getLogin()));
    }

    /**
     * GET /users : get all users.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body all users
     */
    @GetMapping("/users")
    @Timed
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public ResponseEntity<List<UserWithDetailDTO>> getAllUsers(Pageable pageable, @RequestParam Map<String, String> params) {
        log.debug("REST request to get a page of Users, {}", params);

        Map<String, String> searchFacets = RequestParamUtil.removePageableParams(params);

        Page<UserWithDetailDTO> page;
        if (searchFacets.isEmpty()) {
            page = userWithDetailService.getAllManagedUsers(pageable).map(UserWithDetailDTO::new);
        } else {
            page = userWithDetailService.findAllByPagingCriteria(pageable, searchFacets).map(UserWithDetailDTO::new);
        }

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * @return a string list of the all of the authorities
     */
    @GetMapping("/users/authorities")
    @Timed
    public List<String> getAuthorities() {
        return userService.getAuthorities();
    }


    /**
     * @return a string list of the all of the applications
     */
    @GetMapping("/users/applications")
    @Timed
    public List<UserApplicationAccess> getApplications() {
        return userWithDetailService.getUserApplicationAccess(SecurityUtils.getCurrentUserId());
    }

    /**
     * GET /users/:login : get the "login" user.
     *
     * @param login the login of the user to find
     * @return the ResponseEntity with status 200 (OK) and with body the "login" user, or with status 404 (Not Found)
     */
    @GetMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    public ResponseEntity<UserWithDetailDTO> getUser(@PathVariable String login) {
        log.debug("REST request to get User : {}", login);
        return ResponseUtil.wrapOrNotFound(userWithDetailService.getUserByLogin(login).map(UserWithDetailDTO::new));
    }

    /**
     * DELETE /users/:login : delete the "login" User.
     *
     * @param login the login of the user to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    @PreAuthorize(ACCESS_ADMIN)
    public ResponseEntity<Void> deleteUser(@PathVariable String login) {
        log.debug("REST request to delete User: {}", login);
        userWithDetailService.deleteUserByLogin(login);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert( "userManagement.deleted", login)).build();
    }

    /**
     * Get the session timeout configured for the current user's customer.
     *
     * This is for use by the UI to for displaying an alert, auto-logging out and redirecting to the login page.
     *
     * @return the timeout in seconds
     */
    @GetMapping("/users/session-timeout")
    @Timed
    public Integer getUserSessionTimeout() {
        Optional<String> maybeUserLogin = SecurityUtils.getCurrentUserLogin();
        if (maybeUserLogin.isPresent()) {
            Optional<UserWithDetail> maybeUserWithDetail = userWithDetailService.getUserByLoginNoAuth(maybeUserLogin.get());
            if (maybeUserWithDetail.isPresent()) {
                return customerService.findSessionTimeoutNoAuth(maybeUserWithDetail.get().getCustomer().getId());
            }
        }

        // TODO: return a 404 instead of returning a default value?
        // TODO: better place to store this default (it's in the gateway app config yml and the customer entity, not sure where's ideal...)
        // we can't find the user or the customer timeout return a default of 30mins?
        int SESSION_TIMEOUT_S_DEFAULT = 1800;
        return SESSION_TIMEOUT_S_DEFAULT;
    }

    @PostMapping("/users/password-expired")
    @Timed
    public boolean isPasswordExpired(@RequestBody Map<String,String> body) {
        if (!userWithDetailService.isLoginCredentialsCorrect(body.get("username"), body.get("password"))) {
            return false;
        }

        return userWithDetailService.checkUserPasswordExpired(body.get("username"));
    }
}
