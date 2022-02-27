package com.trss.bi.security;

import com.trss.bi.domain.Customer;
import com.trss.bi.domain.CustomerStatus;
import com.trss.bi.domain.User;
import com.trss.bi.domain.UserDetail;
import com.trss.bi.repository.UserDetailRepository;
import com.trss.bi.repository.UserRepository;
import com.trss.bi.service.UserWithDetailService;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UserRepository userRepository;

    private final UserDetailRepository userDetailRepository;

    public DomainUserDetailsService(UserRepository userRepository, UserDetailRepository userDetailRepository) {
        this.userRepository = userRepository;
        this.userDetailRepository = userDetailRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating {}", login);

        if (new EmailValidator().isValid(login, null)) {
            Optional<User> userByEmailFromDatabase = userRepository.findOneWithRoleByEmail(login);
            return userByEmailFromDatabase.map(user -> createSpringSecurityUser(login, user))
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + login + " was not found in the database"));
        }

        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
        Optional<User> userByLoginFromDatabase = userRepository.findOneByLogin(lowercaseLogin);
        return userByLoginFromDatabase.map(user -> createSpringSecurityUser(lowercaseLogin, user))
            .orElseThrow(() -> new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database"));

    }

    private void validateUser(String lowercaseLogin, User user) {
        // retrieve the user with detail to have access to everything the user has
        UserDetail userDetail = userDetailRepository.findById(user.getId()).orElseThrow(() -> new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database"));

        // TODO: should we re-retrieve the customer to make sure we have the latest?
        Customer customer = userDetail.getCustomer();

        // Check Customer has not been soft deleted
        if (customer.getDeleted()) {
            throw new UserCustomerDeletedException("User's customer has been deleted");
        }

        // Check User has not been soft deleted
        if (userDetail.getDeleted()) {
            throw new UserDeletedException("User has been deleted");
        }

        // Check User is Activated
        if (!user.getActivated()) {
            throw new UserNotActivatedException("User " + lowercaseLogin + " was not activated");
        }

        // Check Customer is Active
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new UserInactiveCustomerException("User " + lowercaseLogin + " was not activated");
        }

        // Check User's password has not expired
        UserWithDetailService.checkPasswordExpired(userDetail.getPasswordDate(), customer.getPasswordExpiration());
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(String lowercaseLogin, User user) {
        // Validate User First
        validateUser(lowercaseLogin, user);
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        grantedAuthorities.add(user.getRole());
        grantedAuthorities.addAll(user.getRole().getAuthorities());
        return (org.springframework.security.core.userdetails.User)org.springframework.security.core.userdetails.User.withUsername(user.getLogin())
            .authorities(grantedAuthorities)
            .password(user.getPassword())
            .build();
    }
}
