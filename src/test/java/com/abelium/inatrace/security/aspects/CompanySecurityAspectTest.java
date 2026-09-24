package com.abelium.inatrace.security.aspects;

import com.abelium.inatrace.security.annotations.RequireCompanyAccess;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CompanySecurityAspectTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Long> typedQuery;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private CompanySecurityAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new CompanySecurityAspect(entityManager);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // Dummy target class for reflection
    static class TestController {
        @RequireCompanyAccess(paramName = "companyId")
        public void testMethodWithParam(Long companyId) {}

        @RequireCompanyAccess(paramName = "id")
        public void testMethodWithId(Long id) {}

        @RequireCompanyAccess
        public void testMethodWithDto(TestDto dto) {}
    }

    static class TestDto {
        private Long companyId;
        public TestDto(Long companyId) { this.companyId = companyId; }
        public Long getCompanyId() { return companyId; }
    }

    private void setupMethodCall(String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        Method method = TestController.class.getMethod(methodName, paramTypes);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
    }

    @Test
    void shouldDenyAccess_whenUnauthenticated() throws NoSuchMethodException {
        setupMethodCall("testMethodWithParam", Long.class);
        RequireCompanyAccess annotation = TestController.class.getMethod("testMethodWithParam", Long.class)
                .getAnnotation(RequireCompanyAccess.class);

        assertThrows(AccessDeniedException.class, () -> aspect.validateCompanyAccess(joinPoint, annotation));
    }

    @Test
    void shouldAllowAccess_whenUserIsSystemAdmin() throws NoSuchMethodException {
        setupMethodCall("testMethodWithParam", Long.class);
        RequireCompanyAccess annotation = TestController.class.getMethod("testMethodWithParam", Long.class)
                .getAnnotation(RequireCompanyAccess.class);

        CustomUserDetails admin = new CustomUserDetails(1L, "admin@test.com", "Admin", "User", UserRole.SYSTEM_ADMIN);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, "token", admin.getAuthorities())
        );

        assertDoesNotThrow(() -> aspect.validateCompanyAccess(joinPoint, annotation));
        verify(entityManager, never()).createQuery(anyString(), eq(Long.class));
    }

    @Test
    void shouldAllowAccess_whenUserIsEnrolledInCompany() throws NoSuchMethodException {
        setupMethodCall("testMethodWithParam", Long.class);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"companyId"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{10L});

        RequireCompanyAccess annotation = TestController.class.getMethod("testMethodWithParam", Long.class)
                .getAnnotation(RequireCompanyAccess.class);

        CustomUserDetails user = new CustomUserDetails(42L, "user@test.com", "Regular", "User", UserRole.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, "token", user.getAuthorities())
        );

        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("companyId", 10L)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", 42L)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(1L);

        assertDoesNotThrow(() -> aspect.validateCompanyAccess(joinPoint, annotation));
    }

    @Test
    void shouldThrowAccessDenied_whenUserIsNotEnrolledInCompany() throws NoSuchMethodException {
        setupMethodCall("testMethodWithParam", Long.class);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"companyId"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{10L});

        RequireCompanyAccess annotation = TestController.class.getMethod("testMethodWithParam", Long.class)
                .getAnnotation(RequireCompanyAccess.class);

        CustomUserDetails user = new CustomUserDetails(42L, "user@test.com", "Regular", "User", UserRole.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, "token", user.getAuthorities())
        );

        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("companyId", 10L)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", 42L)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(0L);

        assertThrows(AccessDeniedException.class, () -> aspect.validateCompanyAccess(joinPoint, annotation));
    }

    @Test
    void shouldExtractCompanyId_fromIdParam() throws NoSuchMethodException {
        setupMethodCall("testMethodWithId", Long.class);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"id"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{99L});

        RequireCompanyAccess annotation = TestController.class.getMethod("testMethodWithId", Long.class)
                .getAnnotation(RequireCompanyAccess.class);

        CustomUserDetails user = new CustomUserDetails(42L, "user@test.com", "Regular", "User", UserRole.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, "token", user.getAuthorities())
        );

        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("companyId", 99L)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", 42L)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(1L);

        assertDoesNotThrow(() -> aspect.validateCompanyAccess(joinPoint, annotation));
    }

    @Test
    void shouldExtractCompanyId_fromDtoObject() throws NoSuchMethodException {
        setupMethodCall("testMethodWithDto", TestDto.class);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"dto"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{new TestDto(77L)});

        RequireCompanyAccess annotation = TestController.class.getMethod("testMethodWithDto", TestDto.class)
                .getAnnotation(RequireCompanyAccess.class);

        CustomUserDetails user = new CustomUserDetails(42L, "user@test.com", "Regular", "User", UserRole.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, "token", user.getAuthorities())
        );

        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("companyId", 77L)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", 42L)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(1L);

        assertDoesNotThrow(() -> aspect.validateCompanyAccess(joinPoint, annotation));
    }

    @Test
    void shouldAllowAccess_whenJwtClaimsContainCompanyId() throws NoSuchMethodException {
        setupMethodCall("testMethodWithParam", Long.class);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"companyId"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{55L});

        RequireCompanyAccess annotation = TestController.class.getMethod("testMethodWithParam", Long.class)
                .getAnnotation(RequireCompanyAccess.class);

        CustomUserDetails user = new CustomUserDetails(42L, "user@test.com", "Regular", "User", UserRole.USER);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaims()).thenReturn(Map.of("company_ids", List.of("55", "56")));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, jwt, user.getAuthorities())
        );

        assertDoesNotThrow(() -> aspect.validateCompanyAccess(joinPoint, annotation));
        verify(entityManager, never()).createQuery(anyString(), eq(Long.class));
    }
}
