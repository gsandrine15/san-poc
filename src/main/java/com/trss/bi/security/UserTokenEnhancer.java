package com.trss.bi.security;

import com.trss.bi.domain.ApplicationAssignmentStatus;
import com.trss.bi.domain.UserApplicationAccess;
import com.trss.bi.domain.UserWithDetail;
import com.trss.bi.service.CustomerService;
import com.trss.bi.service.UserWithDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Adds minimal user information to JWT Token to prevent excessive lookups in microservices.
 */
@Component
public class UserTokenEnhancer implements TokenEnhancer {

    @Autowired
    private UserWithDetailService userWithDetailService;

    @Autowired
    private CustomerService customerService;

    /**
     * Enhance token with additional information
     *
     * @param accessToken OAuth2AccessToken
     * @param authentication OAuth2Authentication
     * @return updated OAuth2AccessToken
     */
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Optional<UserWithDetail> maybeUserWithDetail = getCurrentUser(authentication);
        if (maybeUserWithDetail.isPresent()) {
            UserWithDetail userWithDetail = maybeUserWithDetail.get();
            addAdditionalInformation((DefaultOAuth2AccessToken) accessToken, userWithDetail);
            addScopes((DefaultOAuth2AccessToken) accessToken, userWithDetail);
        }

        return accessToken;
    }

    /**
     * Extract user from authentication and lookup from UserWithDetailService
     * Note: SecurityContext and SecurityUtils both point to the wrong principal at this point, so it must come from the auth
     *
     * @param authentication OAuth2AccessToken
     * @return optional UserWithDetail
     */
    private Optional<UserWithDetail> getCurrentUser(OAuth2Authentication authentication) {
        // Lookup information about the current user
        //
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) authentication.getUserAuthentication().getPrincipal();
        String login = principal.getUsername();
        return  userWithDetailService.getUserByLoginNoAuth(login);
    }

    /**
     * Add additional information for this user to the OAuth token
     *
     * @param token DefaultOAuth2AccessToken
     * @param userWithDetail UserWithDetail for current user
     */
    private void addAdditionalInformation(DefaultOAuth2AccessToken token, UserWithDetail userWithDetail) {
        Map<String, Object> additionalInformation = token.getAdditionalInformation();
        if (additionalInformation.isEmpty()) {
            additionalInformation = new LinkedHashMap<>();
        }

        // Add user_id to the token
        additionalInformation.put("user_id", userWithDetail.getId());

        // Add customer_id to the token
        if (userWithDetail.getCustomer() != null && userWithDetail.getCustomer().getId() != null) {
            additionalInformation.put("customer_id", userWithDetail.getCustomer().getId());
            Integer sessionTimeoutS = customerService.findSessionTimeoutNoAuth(userWithDetail.getCustomer().getId());
            additionalInformation.put("customer_session_timeout_seconds", sessionTimeoutS);
        }
        token.setAdditionalInformation(additionalInformation);
    }

    /**
     * Add application scopes to the OAuth token
     *
     * @param token DefaultOAuth2AccessToken
     * @param userWithDetail UserWithDetail for current user
     */
    private void addScopes(DefaultOAuth2AccessToken token, UserWithDetail userWithDetail) {
        Set<String> scopes = new HashSet<>(token.getScope());
        List<UserApplicationAccess> applicationUserAccess = userWithDetailService.getUserApplicationsByStatus(userWithDetail, ApplicationAssignmentStatus.AVAILABLE);
        applicationUserAccess.forEach(a -> scopes.add(a.getApplication().getCode()));
        token.setScope(scopes);
    }
}
