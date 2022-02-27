package com.trss.bi.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.trss.bi.config.ApplicationProperties;
import com.trss.bi.domain.User;
import com.trss.bi.domain.UserWithDetail;
import com.trss.bi.repository.UserRepository;
import com.trss.bi.security.SecurityUtils;
import com.trss.bi.service.MailService;
import com.trss.bi.service.UserService;
import com.trss.bi.service.UserWithDetailService;
import com.trss.bi.service.dto.PasswordChangeDTO;
import com.trss.bi.service.dto.UserDTO;
import com.trss.bi.service.dto.UserWithDetailDTO;
import com.trss.bi.web.rest.errors.*;
import com.trss.bi.web.rest.vm.KeyAndPasswordVM;
import com.trss.bi.web.rest.vm.ManagedUserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;
import java.util.Optional;

import static com.trss.bi.security.AuthorizationConstants.ACCESS_CUSTOMER_ADMIN;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    private final UserRepository userRepository;

    private final UserService userService;

    private final UserWithDetailService userWithDetailService;

    private final MailService mailService;

    private final ApplicationProperties applicationProperties;

    public AccountResource(UserRepository userRepository, UserService userService, UserWithDetailService userWithDetailService, MailService mailService, ApplicationProperties applicationProperties) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.userWithDetailService = userWithDetailService;
        this.mailService = mailService;
        this.applicationProperties = applicationProperties;
    }

    /**
     * POST  /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already used
     * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is already used
     */
    @PostMapping("/register")
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
        userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase()).ifPresent(u -> {throw new LoginAlreadyUsedException();});
        userRepository.findOneByEmailIgnoreCase(managedUserVM.getEmail()).ifPresent(u -> {throw new EmailAlreadyUsedException();});
        User user = userService.registerUser(managedUserVM, managedUserVM.getPassword());
        mailService.sendActivationEmail(user);
    }

    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be activated
     */
    @GetMapping("/activate")
    @Timed
    public void activateAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new InternalServerErrorException("No user was found for this activation key");
        }
    }

    /**
     * GET  /authenticate : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping("/authenticate")
    @Timed
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * GET  /account : get the current user.
     *
     * @return the current user
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be returned
     */
    @GetMapping("/account")
    @Timed
    public UserDTO getAccount() {
        return userService.getUserWithRole()
            .map(UserDTO::new)
            .orElseThrow(() -> new InternalServerErrorException("User could not be found"));
    }

    @GetMapping("/account-user-detail")
    @Timed
    public UserWithDetailDTO getAccountUserDetail() {
        Optional<UserWithDetail> maybeUserWithDetail = SecurityUtils.getCurrentUserLogin()
            .map(userWithDetailService::getUserByLoginNoAuth)
            .orElseThrow(() -> new InternalServerErrorException("User could not be found"));

        if (!maybeUserWithDetail.isPresent()) {
            throw new InternalServerErrorException("User could not be found");
        }

        return new UserWithDetailDTO(maybeUserWithDetail.get());
    }

    @PostMapping("/account-user-detail")
    @Timed
    public void saveAccountUserDetail(@RequestBody UserWithDetailDTO userWithDetailDTO) {
        final String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new InternalServerErrorException("Current user login not found"));

        if (userWithDetailDTO.getEmail() != null) {
            Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userWithDetailDTO.getEmail());
            if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userLogin))) {
                throw new EmailAlreadyUsedException();
            }
        }

        Optional<User> user = userRepository.findOneByLogin(userLogin);
        if (!user.isPresent()) {
            throw new InternalServerErrorException("User could not be found");
        }

        // only update the firstName, lastName, email and phone number
        userWithDetailService.updateCurrentUser(userWithDetailDTO.getFirstName(), userWithDetailDTO.getLastName(),
                                                userWithDetailDTO.getEmail(), userWithDetailDTO.getPhoneNumber());
    }

    /**
     * POST  /account : update the current user information.
     *
     * @param userDTO the current user information
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already used
     * @throws RuntimeException 500 (Internal Server Error) if the user login wasn't found
     */
    @PostMapping("/account")
    @Timed
    public void saveAccount(@Valid @RequestBody UserDTO userDTO) {
        final String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new InternalServerErrorException("Current user login not found"));

        if (userDTO.getEmail() != null) {
            Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
            if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userLogin))) {
                throw new EmailAlreadyUsedException();
            }
        }

        Optional<User> user = userRepository.findOneByLogin(userLogin);
        if (!user.isPresent()) {
            throw new InternalServerErrorException("User could not be found");
        }
        userService.updateUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(),
            userDTO.getLangKey(), userDTO.getImageUrl());
   }

    /**
     * POST  /account/change-password : changes the current user's password
     *
     * @param passwordChangeDto current and new password
     * @throws InvalidPasswordException 400 (Bad Request) if the new password is incorrect
     */
    @PostMapping(path = "/account/change-password")
    @Timed
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        userWithDetailService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
   }

    /**
     * Update an expired password. Note the user is not logged in when making this call.
     * @param body
     */
    @PostMapping("/account/update-expired-password")
    @Timed
    public void updateExpiredPassword(@RequestBody Map<String,String> body) {
        if (!userWithDetailService.isLoginCredentialsCorrect(body.get("username"), body.get("currentPassword"))) {
            throw new RuntimeException("Credentials are incorrect.");
        }

        userWithDetailService.changePasswordNoAuth(body.get("username"), body.get("currentPassword"), body.get("newPassword"));
    }

    /**
     * POST   /account/reset-password/init : Send an email to reset the password of the user
     *
     * @param mail the mail of the user
     * @throws EmailNotFoundException 400 (Bad Request) if the email address is not registered
     */
    @PostMapping(path = "/account/reset-password/init")
    @Timed
    public void requestPasswordReset(@RequestBody String mail) {
        userService.requestPasswordReset(mail)
            .ifPresent(mailService::sendPasswordResetMail);
    }

    @PostMapping(path = "/account/admin-reset-password/init")
    @Timed
    @PreAuthorize(ACCESS_CUSTOMER_ADMIN)
    public String requestAdminPasswordReset(@RequestBody String username) {
        User user = userService.requestPasswordResetByUsername(username).orElseThrow(() -> new InternalServerErrorException("No active user found"));
        return "/#/reset/finish?key=" + user.getResetKey();
    }

    /**
     * POST   /account/reset-password/finish : Finish to reset the password of the user
     *
     * @param keyAndPassword the generated key and the new password
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws RuntimeException 500 (Internal Server Error) if the password could not be reset
     */
    @PostMapping(path = "/account/reset-password/finish")
    @Timed
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        Optional<User> user =
            userWithDetailService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (!user.isPresent()) {
            throw new InternalServerErrorException("No user was found for this reset key");
        }
    }

    @GetMapping(path = "/account/reset-password/expired")
    public boolean isPasswordResetKeyExpired(@RequestParam String key) {
        return userService.isPasswordResetKeyExpired(key);
    }
}
