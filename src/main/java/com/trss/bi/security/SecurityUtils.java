package com.trss.bi.security;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for Spring Security.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Get the login of the current user.
     *
     * @return the login of the current user
     */
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
            .map(authentication -> {
                if (authentication.getPrincipal() instanceof UserDetails) {
                    UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                    return springSecurityUser.getUsername();
                } else if (authentication.getPrincipal() instanceof String) {
                    return (String) authentication.getPrincipal();
                }
                return null;
            });
    }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise
     * @deprecated no longer used
     */
    @Deprecated
    public static boolean isAuthenticated() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
            .map(authentication -> authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(AuthorizationConstants.ANONYMOUS)))
            .orElse(false);
    }

    /**
     * If the current user has a specific authority (security role).
     * <p>
     * The name of this method comes from the isUserInRole() method in the Servlet API
     *
     * @param authority the authority to check
     * @return true if the current user has the authority, false otherwise
     */
    public static boolean isCurrentUserInRole(String ...authorities) {
        return isCurrentUserIn("ROLE_", authorities);
    }

    /**
     * If the current user has a specific authority.
     * <p>
     *
     * @param authority the authority to check
     * @return true if the current user has the authority, false otherwise
     */
    public static boolean doesCurrentUserHaveAuthority(String ...authorities) {
        return isCurrentUserIn("", authorities);
    }

    /**
     * If the current user has a specific authority.
     * <p>
     *
     * @param prefix the prefix (should be ROLE_ if passed roles, or an empty string is passed authorities)
     * @param authority the authority to check
     * @return true if the current user has the authority, false otherwise
     */
    private static boolean isCurrentUserIn(String prefix, String ...authorities) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        boolean has = false;
        for(String authority : authorities) {
            has = has || Optional.ofNullable(securityContext.getAuthentication())
                .map(authentication -> authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(prefix + authority)))
                .orElse(false);
        }
        return has;
    }

    /**
     * Get the authentication details of the current user
     *
     * @return map of authentication details
     */
    public static Map<String, Object> getCurrentAuthDetails() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if ( securityContext != null && securityContext.getAuthentication() != null) {
            OAuth2Authentication auth = (OAuth2Authentication) securityContext.getAuthentication();
            if (auth.getDetails() != null) {
                OAuth2AuthenticationDetails authDetails = (OAuth2AuthenticationDetails) securityContext.getAuthentication().getDetails();
                if (authDetails.getDecodedDetails() != null) {
                    return (Map<String, Object>) authDetails.getDecodedDetails();
                }
            }
        }
        return null;
    }

    /**
     * Get the user_id for the current user
     *
     * @return Long user_id
     */
    public static Long getCurrentUserId() {
        Map<String, Object> details = getCurrentAuthDetails();
        if (details != null && details.containsKey("user_id")) {
            return Long.parseLong(details.get("user_id").toString());
        }
        else {
            return null;
        }
    }

    /**
     * Get the customer_id for the current user
     *
     * @return Long customer_id
     */
    public static Long getCurrentCustomerId() {
        Map<String, Object> details = getCurrentAuthDetails();
        if (details != null && details.containsKey("customer_id")) {
            return Long.parseLong(details.get("customer_id").toString());
        }
        else {
            return null;
        }
    }

    /**
     * Determines if the current user has administrative rights for this customer
     *
     * @param customerId the id of the customer entity being acted on
     * @return true if the current user has the authority, false otherwise
     */
    public static boolean canAdminCustomer(Long customerId) {
        if (isCurrentUserInRole(AuthorizationConstants.ADMIN)) {
            return true;
        }
        else if (isCurrentUserInRole("CUSTOMER_ADMIN")) {
            return Objects.equals(customerId, getCurrentCustomerId());
        }
        else {
            return false;
        }
    }
}
