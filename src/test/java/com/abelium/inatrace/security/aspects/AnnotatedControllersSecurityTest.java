package com.abelium.inatrace.security.aspects;

import com.abelium.inatrace.components.common.CommonCsvController;
import com.abelium.inatrace.components.company.CompanyController;
import com.abelium.inatrace.components.company.api.ApiCompanyActionRequest;
import com.abelium.inatrace.components.company.types.CompanyAction;
import com.abelium.inatrace.components.dashboard.DashboardController;
import com.abelium.inatrace.components.dashboard.api.ApiAggregationTimeUnit;
import com.abelium.inatrace.components.dashboard.api.ApiExportType;
import com.abelium.inatrace.components.dashboard.api.ApiProcessingPerformanceRequest;
import com.abelium.inatrace.components.groupstockorder.GroupStockOrderController;
import com.abelium.inatrace.components.payment.PaymentController;
import com.abelium.inatrace.components.stockorder.StockOrderController;
import com.abelium.inatrace.security.annotations.RequireCompanyAccess;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.Language;
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

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnnotatedControllersSecurityTest {

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
        when(joinPoint.getSignature()).thenReturn(methodSignature);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateUser(Long userId, String email, UserRole role) {
        CustomUserDetails user = new CustomUserDetails(userId, email, "Test", "User", role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, "credentials", user.getAuthorities())
        );
    }

    private void mockEnrollment(Long companyId, Long userId, boolean enrolled) {
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("companyId", companyId)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", userId)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(enrolled ? 1L : 0L);
    }

    @Test
    void groupStockOrderController_exportExcelByCompany_hasAnnotationAndEnforcesSecurity() throws Exception {
        Method method = GroupStockOrderController.class.getMethod(
                "exportGroupedStockOrdersExcelByCompany", Long.class, Language.class
        );
        RequireCompanyAccess annotation = method.getAnnotation(RequireCompanyAccess.class);
        assertNotNull(annotation, "exportGroupedStockOrdersExcelByCompany must be annotated with @RequireCompanyAccess");

        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"companyId", "language"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{10L, Language.EN});

        // 1. Unauthenticated -> 403 AccessDenied
        assertThrows(AccessDeniedException.class, () -> aspect.validateCompanyAccess(joinPoint, annotation));

        // 2. Authenticated but not enrolled -> 403 AccessDenied
        authenticateUser(42L, "user@test.com", UserRole.USER);
        mockEnrollment(10L, 42L, false);
        assertThrows(AccessDeniedException.class, () -> aspect.validateCompanyAccess(joinPoint, annotation));

        // 3. Authenticated and enrolled -> Allowed
        mockEnrollment(10L, 42L, true);
        assertDoesNotThrow(() -> aspect.validateCompanyAccess(joinPoint, annotation));
    }

    @Test
    void paymentController_exportPaymentsByCompany_enforcesSecurity() throws Exception {
        Method method = PaymentController.class.getMethod(
                "exportPaymentsByCompany", CustomUserDetails.class, Long.class, Language.class
        );
        RequireCompanyAccess annotation = method.getAnnotation(RequireCompanyAccess.class);
        assertNotNull(annotation, "exportPaymentsByCompany must be annotated with @RequireCompanyAccess");

        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"authUser", "companyId", "language"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{null, 25L, Language.EN});

        authenticateUser(42L, "user@test.com", UserRole.USER);
        mockEnrollment(25L, 42L, false);

        assertThrows(AccessDeniedException.class, () -> aspect.validateCompanyAccess(joinPoint, annotation));

        mockEnrollment(25L, 42L, true);
        assertDoesNotThrow(() -> aspect.validateCompanyAccess(joinPoint, annotation));
    }

    @Test
    void stockOrderController_exportDeliveriesByCompany_enforcesSecurity() throws Exception {
        Method method = StockOrderController.class.getMethod(
                "exportDeliveriesByCompany", CustomUserDetails.class, Long.class, Language.class
        );
        RequireCompanyAccess annotation = method.getAnnotation(RequireCompanyAccess.class);
        assertNotNull(annotation, "exportDeliveriesByCompany must be annotated with @RequireCompanyAccess");

        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"authUser", "companyId", "language"});
        when(joinPoint.getArgs()).thenReturn(new Object[]{null, 33L, Language.EN});

        authenticateUser(42L, "user@test.com", UserRole.USER);
        mockEnrollment(33L, 42L, false);

        assertThrows(AccessDeniedException.class, () -> aspect.validateCompanyAccess(joinPoint, annotation));

        mockEnrollment(33L, 42L, true);
        assertDoesNotThrow(() -> aspect.validateCompanyAccess(joinPoint, annotation));
    }

    @Test
    void dashboardController_classLevelAnnotation_enforcesSecurityOnDtoBody() throws Exception {
        RequireCompanyAccess classAnnotation = DashboardController.class.getAnnotation(RequireCompanyAccess.class);
        assertNotNull(classAnnotation, "DashboardController must have class-level @RequireCompanyAccess");

        Method method = DashboardController.class.getMethod(
                "calculateProcessingPerformanceData", ApiProcessingPerformanceRequest.class
        );

        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"processingPerformanceRequest"});

        ApiProcessingPerformanceRequest request = new ApiProcessingPerformanceRequest(
                50L, null, null, null, null, Collections.emptyList(), ApiAggregationTimeUnit.MONTH, ApiExportType.EXCEL
        );
        when(joinPoint.getArgs()).thenReturn(new Object[]{request});

        authenticateUser(42L, "user@test.com", UserRole.USER);
        mockEnrollment(50L, 42L, false);

        assertThrows(AccessDeniedException.class, () -> aspect.validateCompanyAccess(joinPoint, classAnnotation));

        mockEnrollment(50L, 42L, true);
        assertDoesNotThrow(() -> aspect.validateCompanyAccess(joinPoint, classAnnotation));
    }

    @Test
    void companyController_executeAction_enforcesSecurityOnActionDto() throws Exception {
        Method method = CompanyController.class.getMethod(
                "executeAction", CustomUserDetails.class, ApiCompanyActionRequest.class, CompanyAction.class
        );
        RequireCompanyAccess annotation = method.getAnnotation(RequireCompanyAccess.class);
        assertNotNull(annotation, "CompanyController.executeAction must be annotated with @RequireCompanyAccess");

        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"authUser", "request", "action"});

        ApiCompanyActionRequest request = new ApiCompanyActionRequest();
        request.companyId = 99L;
        when(joinPoint.getArgs()).thenReturn(new Object[]{null, request, CompanyAction.ACTIVATE_COMPANY});

        authenticateUser(42L, "user@test.com", UserRole.USER);
        mockEnrollment(99L, 42L, false);

        assertThrows(AccessDeniedException.class, () -> aspect.validateCompanyAccess(joinPoint, annotation));

        mockEnrollment(99L, 42L, true);
        assertDoesNotThrow(() -> aspect.validateCompanyAccess(joinPoint, annotation));
    }
}
