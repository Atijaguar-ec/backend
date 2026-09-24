package com.abelium.inatrace.security.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce multi-tenant isolation by checking user company enrollment (HU-02, ADR-007, ADR-009).
 * Methods annotated with @RequireCompanyAccess will be intercepted by CompanySecurityAspect,
 * which extracts the target companyId (from method parameters or request body) and validates that
 * the authenticated user is enrolled in that company or possesses SYSTEM_ADMIN privileges.
 * If unauthorized, an AccessDeniedException is thrown resulting in HTTP 403 Forbidden.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireCompanyAccess {

    /**
     * Name of the parameter representing the company ID.
     * If blank (default), the aspect will automatically inspect parameter names
     * ("companyId", "id", "quoteCompanyId") or request body / query object getters.
     */
    String paramName() default "";

    /**
     * Optional field path or property name within an object parameter (e.g. "companyId").
     */
    String expression() default "";
}
