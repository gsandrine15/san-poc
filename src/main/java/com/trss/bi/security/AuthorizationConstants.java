package com.trss.bi.security;

import java.util.Arrays;
import java.util.List;

/**
 * Constants for Spring Security authorizations.
 */
public final class AuthorizationConstants {
    public static final String ADMIN = "ADMIN";
    public static final String CUSTOMER_ADMIN = "CUSTOMER_ADMIN";
    public static final String USER = "USER";
    public static final String NO_PERMISSIONS = "NO_PERMISSIONS";
    public static final List<String> RESERVED_ROLES = Arrays.asList(ADMIN, CUSTOMER_ADMIN, USER, NO_PERMISSIONS);

    /**
     * @deprecated no longer valid
     */
    @Deprecated
    public static final String CLIENT_VIEW_EDIT_USER = "CLIENT_VIEW_EDIT_USER";
    /**
     * @deprecated no longer valid
     */
    @Deprecated
    public static final String CLIENT_VIEW_ONLY_USER = "CLIENT_VIEW_ONLY_USER";

    /**
     * @deprecated no longer valid
     */
    @Deprecated
    public static final String ANONYMOUS = "ANONYMOUS";

    public static final String ACCESS_CUSTOMER_ADMIN = "hasAnyRole('CUSTOMER_ADMIN', '" + ADMIN + "')";

    public static final String ACCESS_ADMIN = "hasAnyRole('" + ADMIN + "')";
}
