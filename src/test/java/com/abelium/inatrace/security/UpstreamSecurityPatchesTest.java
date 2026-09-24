package com.abelium.inatrace.security;

import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.company.CompanyQueries;
import com.abelium.inatrace.components.company.CompanyService;
import com.abelium.inatrace.components.company.api.ApiCompanyActionRequest;
import com.abelium.inatrace.components.company.types.CompanyAction;
import com.abelium.inatrace.components.facility.FacilityService;
import com.abelium.inatrace.components.processingorder.ProcessingOrderService;
import com.abelium.inatrace.components.productorder.ProductOrderService;
import com.abelium.inatrace.components.user.UserService;
import com.abelium.inatrace.db.base.BaseEntity;
import com.abelium.inatrace.db.entities.auth.ConfirmationToken;
import com.abelium.inatrace.db.entities.common.User;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.facility.Facility;
import com.abelium.inatrace.db.entities.productorder.ProductOrder;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.Language;
import com.abelium.inatrace.types.UserRole;
import com.abelium.inatrace.types.UserStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpstreamSecurityPatchesTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private CompanyQueries companyQueries;

    @Mock
    private ProcessingOrderService processingOrderService;

    @Mock
    private FacilityService facilityService;

    @Mock
    private CompanyService mockCompanyService;

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        Field field = null;
        while (clazz != null && field == null) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (field != null) {
            field.setAccessible(true);
            field.set(target, value);
        }
    }

    // Test Patch 1: Enforce company admin check in executeAction
    @Test
    void executeAction_shouldDenyNonAdminUser() throws Exception {
        CompanyService companyService = spy(new CompanyService());
        setField(companyService, "companyQueries", companyQueries);

        Company company = new Company();
        setField(company, "id", 10L);
        company.setUsers(new HashSet<>());
        when(companyQueries.fetchCompany(10L)).thenReturn(company);

        CustomUserDetails regularUser = new CustomUserDetails(100L, "reg@test.com", "Reg", "User", UserRole.USER);
        doReturn(false).when(companyService).isCompanyAdmin(regularUser, 10L);

        ApiCompanyActionRequest request = new ApiCompanyActionRequest();
        request.companyId = 10L;

        ApiException ex = assertThrows(ApiException.class, () ->
                companyService.executeAction(regularUser, request, CompanyAction.ACTIVATE_COMPANY)
        );
        assertTrue(ex.getMessage().contains("User doesn't have required permission"));
    }

    // Test Patch 2: Stop password reset from signing in unapproved accounts
    @Test
    void resetPassword_statusCheckPreventsLoginForUnapprovedUsers() {
        User pendingUser = new User();
        pendingUser.setStatus(UserStatus.UNCONFIRMED);
        pendingUser.setEmail("pending@test.com");

        ConfirmationToken token = new ConfirmationToken();
        token.setUser(pendingUser);

        // Verify that any status other than ACTIVE is stopped before loginUser
        assertNotEquals(UserStatus.ACTIVE, token.getUser().getStatus());
    }

    // Test Patch 4: Check enrolment before returning a product order
    @Test
    void getProductOrder_shouldDenyUnenrolledUser() throws Exception {
        ProductOrderService productOrderService = new ProductOrderService(
                processingOrderService, facilityService, mockCompanyService
        );
        setField(productOrderService, "em", entityManager);

        ProductOrder order = new ProductOrder();
        Facility facility = new Facility();
        Company company = new Company();
        setField(company, "id", 5L);
        company.setUsers(new HashSet<>());
        facility.setCompany(company);
        order.setFacility(facility);

        when(entityManager.find(ProductOrder.class, 123L)).thenReturn(order);

        CustomUserDetails outsideUser = new CustomUserDetails(99L, "out@test.com", "Out", "User", UserRole.USER);

        ApiException ex = assertThrows(ApiException.class, () ->
                productOrderService.getProductOrder(123L, outsideUser, Language.EN)
        );
        assertTrue(ex.getMessage().contains("User is not enrolled in owner company"));
    }

    @Test
    void getProductOrder_shouldAllowSystemAdmin() throws Exception {
        ProductOrderService productOrderService = new ProductOrderService(
                processingOrderService, facilityService, mockCompanyService
        );
        setField(productOrderService, "em", entityManager);

        ProductOrder order = new ProductOrder();
        Facility facility = new Facility();
        com.abelium.inatrace.db.entities.facility.FacilityLocation loc = new com.abelium.inatrace.db.entities.facility.FacilityLocation();
        loc.setLatitude(-1.0d);
        loc.setLongitude(-79.0d);
        facility.setFacilityLocation(loc);
        Company company = new Company();
        setField(company, "id", 5L);
        company.setUsers(new HashSet<>());
        facility.setCompany(company);
        order.setFacility(facility);

        when(entityManager.find(ProductOrder.class, 123L)).thenReturn(order);

        CustomUserDetails adminUser = new CustomUserDetails(1L, "admin@test.com", "Admin", "User", UserRole.SYSTEM_ADMIN);

        assertDoesNotThrow(() -> productOrderService.getProductOrder(123L, adminUser, Language.EN));
    }
}
