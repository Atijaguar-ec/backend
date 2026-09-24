package com.abelium.inatrace.security.aspects;

import com.abelium.inatrace.security.annotations.RequireCompanyAccess;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.UserRole;
import jakarta.persistence.EntityManager;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;

/**
 * AOP Aspect intercepting methods annotated with @RequireCompanyAccess (HU-02, ADR-007, ADR-009).
 * Enforces systematic multi-tenant isolation by extracting companyId and verifying enrollment.
 */
@Aspect
@Component
@Order(1)
public class CompanySecurityAspect {

    private static final Logger logger = LoggerFactory.getLogger(CompanySecurityAspect.class);

    private final EntityManager entityManager;

    @Autowired
    public CompanySecurityAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Before("@annotation(requireCompanyAccess) || @within(requireCompanyAccess)")
    public void validateCompanyAccess(JoinPoint joinPoint, RequireCompanyAccess requireCompanyAccess) {
        // If annotation was placed at class level and method doesn't have it directly, resolve from class
        if (requireCompanyAccess == null) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            requireCompanyAccess = signature.getMethod().getAnnotation(RequireCompanyAccess.class);
            if (requireCompanyAccess == null) {
                requireCompanyAccess = joinPoint.getTarget().getClass().getAnnotation(RequireCompanyAccess.class);
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Full authentication is required to access company resources");
        }

        CustomUserDetails userDetails = extractCustomUserDetails(authentication, joinPoint.getArgs());

        // System administrators have global cross-company access
        if (isSystemAdmin(authentication, userDetails)) {
            logger.debug("System admin access granted to company resource for user {}", authentication.getName());
            return;
        }

        Long companyId = extractCompanyId(joinPoint, requireCompanyAccess);
        if (companyId == null) {
            logger.error("Could not resolve companyId for method {}", joinPoint.getSignature().toShortString());
            throw new AccessDeniedException("Could not determine companyId for tenant validation");
        }

        // Validate Keycloak JWT claims if present
        if (isAuthorizedViaJwtClaims(authentication, companyId)) {
            logger.debug("User {} authorized for company {} via JWT claims", authentication.getName(), companyId);
            return;
        }

        // Authoritative Database Multi-Tenant Validation
        if (userDetails != null && userDetails.getUserId() != null) {
            Long enrollmentCount = entityManager.createQuery(
                    "SELECT COUNT(cu) FROM CompanyUser cu WHERE cu.company.id = :companyId AND cu.user.id = :userId",
                    Long.class
            ).setParameter("companyId", companyId)
             .setParameter("userId", userDetails.getUserId())
             .getSingleResult();

            if (enrollmentCount != null && enrollmentCount > 0) {
                logger.debug("User {} (id: {}) verified for company {}", userDetails.getUsername(), userDetails.getUserId(), companyId);
                return;
            }
        }

        logger.warn("SECURITY ALERT (HU-02): Unauthorized multi-tenant access attempt! User '{}' attempted to access company {}",
                authentication.getName(), companyId);
        throw new AccessDeniedException("User is not authorized to access company " + companyId);
    }

    private boolean isSystemAdmin(Authentication authentication, CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getUserRole() == UserRole.SYSTEM_ADMIN) {
            return true;
        }
        return authentication.getAuthorities().stream().anyMatch(authority ->
                "ROLE_SYSTEM_ADMIN".equalsIgnoreCase(authority.getAuthority()) ||
                "SYSTEM_ADMIN".equalsIgnoreCase(authority.getAuthority())
        );
    }

    private CustomUserDetails extractCustomUserDetails(Authentication authentication, Object[] args) {
        if (authentication.getPrincipal() instanceof CustomUserDetails cud) {
            return cud;
        }
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof CustomUserDetails cud) {
                    return cud;
                }
            }
        }
        return null;
    }

    private boolean isAuthorizedViaJwtClaims(Authentication authentication, Long companyId) {
        Jwt jwt = null;
        if (authentication.getPrincipal() instanceof Jwt j) {
            jwt = j;
        } else if (authentication.getCredentials() instanceof Jwt j) {
            jwt = j;
        }

        if (jwt != null) {
            Object companyClaims = jwt.getClaims().get("company_ids");
            if (companyClaims instanceof Collection<?> col) {
                for (Object item : col) {
                    if (companyId.toString().equals(String.valueOf(item))) {
                        return true;
                    }
                }
            }
            Object singleCompanyClaim = jwt.getClaims().get("company_id");
            if (singleCompanyClaim != null && companyId.toString().equals(String.valueOf(singleCompanyClaim))) {
                return true;
            }
        }
        return false;
    }

    private Long extractCompanyId(JoinPoint joinPoint, RequireCompanyAccess annotation) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = signature.getMethod() != null ? signature.getMethod().getParameters() : null;

        String targetParam = resolveTargetParam(annotation);

        // 1. If explicit target parameter is specified, check paramNames and Spring web annotations
        if (!targetParam.isEmpty()) {
            if (paramNames != null && args != null) {
                for (int i = 0; i < Math.min(paramNames.length, args.length); i++) {
                    if (paramNames[i].equalsIgnoreCase(targetParam)) {
                        Long val = toLong(args[i]);
                        if (val != null) return val;
                    }
                }
            }
            if (parameters != null && args != null) {
                for (int i = 0; i < Math.min(parameters.length, args.length); i++) {
                    if (matchesTargetParam(parameters[i], targetParam)) {
                        Long val = toLong(args[i]);
                        if (val != null) return val;
                    }
                }
            }
        }

        // 2. High-priority standard company parameters (companyId, quoteCompanyId)
        if (paramNames != null && args != null) {
            for (int i = 0; i < Math.min(paramNames.length, args.length); i++) {
                String name = paramNames[i].toLowerCase();
                if (name.equals("companyid") || name.equals("quotecompanyid")) {
                    Long val = toLong(args[i]);
                    if (val != null) return val;
                }
            }
        }
        if (parameters != null && args != null) {
            for (int i = 0; i < Math.min(parameters.length, args.length); i++) {
                if (matchesStandardCompanyParam(parameters[i])) {
                    Long val = toLong(args[i]);
                    if (val != null) return val;
                }
            }
        }

        // 3. Inspect objects / DTOs in args
        if (args != null) {
            for (Object arg : args) {
                if (arg != null && !isPrimitiveOrStandard(arg)) {
                    Long val = extractCompanyIdFromObject(arg, annotation != null ? annotation.expression() : null);
                    if (val != null) return val;
                }
            }
        }

        // 4. Low-priority fallback: generic "id" parameter (only if no explicit companyId param was found)
        if (paramNames != null && args != null) {
            for (int i = 0; i < Math.min(paramNames.length, args.length); i++) {
                if (paramNames[i].equalsIgnoreCase("id")) {
                    Long val = toLong(args[i]);
                    if (val != null) return val;
                }
            }
        }
        if (parameters != null && args != null) {
            for (int i = 0; i < Math.min(parameters.length, args.length); i++) {
                if (matchesIdParam(parameters[i])) {
                    Long val = toLong(args[i]);
                    if (val != null) return val;
                }
            }
        }

        return null;
    }

    private String resolveTargetParam(RequireCompanyAccess annotation) {
        if (annotation == null) return "";
        if (!annotation.paramName().trim().isEmpty()) return annotation.paramName().trim();
        if (!annotation.companyIdParam().trim().isEmpty()) return annotation.companyIdParam().trim();
        if (!annotation.value().trim().isEmpty()) return annotation.value().trim();
        return "";
    }

    private boolean matchesTargetParam(Parameter param, String targetParam) {
        var pathVar = param.getAnnotation(org.springframework.web.bind.annotation.PathVariable.class);
        if (pathVar != null) {
            String name = pathVar.value().isEmpty() ? pathVar.name() : pathVar.value();
            if (targetParam.equalsIgnoreCase(name)) return true;
        }
        var reqParam = param.getAnnotation(org.springframework.web.bind.annotation.RequestParam.class);
        if (reqParam != null) {
            String name = reqParam.value().isEmpty() ? reqParam.name() : reqParam.value();
            if (targetParam.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private boolean matchesStandardCompanyParam(Parameter param) {
        var pathVar = param.getAnnotation(org.springframework.web.bind.annotation.PathVariable.class);
        if (pathVar != null) {
            String name = (pathVar.value().isEmpty() ? pathVar.name() : pathVar.value()).toLowerCase();
            if (name.equals("companyid") || name.equals("quotecompanyid")) return true;
        }
        var reqParam = param.getAnnotation(org.springframework.web.bind.annotation.RequestParam.class);
        if (reqParam != null) {
            String name = (reqParam.value().isEmpty() ? reqParam.name() : reqParam.value()).toLowerCase();
            if (name.equals("companyid") || name.equals("quotecompanyid")) return true;
        }
        return false;
    }

    private boolean matchesIdParam(Parameter param) {
        var pathVar = param.getAnnotation(org.springframework.web.bind.annotation.PathVariable.class);
        if (pathVar != null) {
            String name = pathVar.value().isEmpty() ? pathVar.name() : pathVar.value();
            if ("id".equalsIgnoreCase(name)) return true;
        }
        var reqParam = param.getAnnotation(org.springframework.web.bind.annotation.RequestParam.class);
        if (reqParam != null) {
            String name = reqParam.value().isEmpty() ? reqParam.name() : reqParam.value();
            if ("id".equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private Long extractCompanyIdFromObject(Object obj, String expression) {
        if (obj == null) return null;
        Class<?> clazz = obj.getClass();

        if (expression != null && !expression.isBlank()) {
            try {
                String getterName = "get" + Character.toUpperCase(expression.charAt(0)) + expression.substring(1);
                Method m = clazz.getMethod(getterName);
                return toLong(m.invoke(obj));
            } catch (Exception ignored) {}
            try {
                Field f = clazz.getDeclaredField(expression);
                f.setAccessible(true);
                return toLong(f.get(obj));
            } catch (Exception ignored) {}
        }

        // Standard getter: getCompanyId()
        try {
            Method m = clazz.getMethod("getCompanyId");
            Long val = toLong(m.invoke(obj));
            if (val != null) return val;
        } catch (Exception ignored) {}

        // Field: companyId
        try {
            Field f = clazz.getDeclaredField("companyId");
            f.setAccessible(true);
            Long val = toLong(f.get(obj));
            if (val != null) return val;
        } catch (Exception ignored) {}

        return null;
    }

    private boolean isPrimitiveOrStandard(Object obj) {
        if (obj instanceof Number || obj instanceof String || obj instanceof Boolean ||
            obj instanceof Character || obj instanceof Enum<?> || obj instanceof CustomUserDetails ||
            obj instanceof java.time.temporal.Temporal || obj instanceof java.util.Locale) {
            return true;
        }
        String pkg = obj.getClass().getPackageName();
        return pkg.startsWith("java.") || pkg.startsWith("jakarta.servlet") || pkg.startsWith("org.springframework");
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
