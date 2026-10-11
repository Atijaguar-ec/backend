package com.abelium.inatrace.components.facility;

import com.abelium.inatrace.components.codebook.semiproduct.SemiProductService;
import com.abelium.inatrace.components.company.CompanyQueries;
import com.abelium.inatrace.components.company.api.ApiAddress;
import com.abelium.inatrace.components.common.api.ApiCountry;
import com.abelium.inatrace.components.facility.api.ApiFacility;
import com.abelium.inatrace.components.facility.api.ApiFacilityLocation;
import com.abelium.inatrace.components.product.FinalProductService;
import com.abelium.inatrace.components.value_chain.ValueChainService;
import com.abelium.inatrace.components.codebook.facility_type.api.ApiFacilityType;
import com.abelium.inatrace.db.entities.codebook.FacilityType;
import com.abelium.inatrace.db.entities.common.Address;
import com.abelium.inatrace.db.entities.common.Country;
import com.abelium.inatrace.db.entities.common.User;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.company.CompanyUser;
import com.abelium.inatrace.db.entities.facility.Facility;
import com.abelium.inatrace.db.entities.facility.FacilityLocation;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.CompanyUserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock
    private SemiProductService semiProductService;
    @Mock
    private CompanyQueries companyQueries;
    @Mock
    private FinalProductService finalProductService;
    @Mock
    private ValueChainService valueChainService;
    @Mock
    private EntityManager entityManager;

    private FacilityService facilityService;

    @BeforeEach
    void setUp() {
        facilityService = new FacilityService(semiProductService, companyQueries, finalProductService, valueChainService);
        ReflectionTestUtils.setField(facilityService, "em", entityManager);
    }

    @Test
    @DisplayName("createOrUpdateFacility: correctly persists displayFinalPriceDiscount, displayMoisturePercentage and level")
    void createOrUpdateFacility_persistsDisplayFinalPriceDiscountAndMoisture() throws Exception {
        Long facilityId = 12L;
        Long companyId = 2L;

        Company company = new Company();
        ReflectionTestUtils.setField(company, "id", companyId);

        User userEntity = new User();
        ReflectionTestUtils.setField(userEntity, "id", 100L);

        CompanyUser companyUser = new CompanyUser();
        companyUser.setUser(userEntity);
        companyUser.setCompany(company);
        companyUser.setRole(CompanyUserRole.COMPANY_ADMIN);
        company.setUsers(Set.of(companyUser));

        Facility existingFacility = new Facility();
        ReflectionTestUtils.setField(existingFacility, "id", facilityId);
        existingFacility.setCompany(company);
        existingFacility.setDisplayFinalPriceDiscount(false);
        existingFacility.setDisplayMoisturePercentage(false);
        existingFacility.setLevel(null);

        FacilityLocation existingLocation = new FacilityLocation();
        Address existingAddress = new Address();
        existingLocation.setAddress(existingAddress);
        existingFacility.setFacilityLocation(existingLocation);

        when(entityManager.find(eq(Facility.class), eq(facilityId))).thenReturn(existingFacility);

        Country country = new Country();
        ReflectionTestUtils.setField(country, "id", 65L);
        when(entityManager.find(eq(Country.class), eq(65L))).thenReturn(country);

        FacilityType facilityType = new FacilityType();
        ReflectionTestUtils.setField(facilityType, "id", 1L);
        when(entityManager.find(eq(FacilityType.class), eq(1L))).thenReturn(facilityType);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUserId()).thenReturn(100L);

        ApiFacility apiFacility = new ApiFacility();
        apiFacility.setId(facilityId);
        apiFacility.setName("Centro de Acopio 2 de Mayo");
        apiFacility.setIsCollectionFacility(true);
        apiFacility.setIsPublic(false);
        apiFacility.setDisplayFinalPriceDiscount(true);
        apiFacility.setDisplayMoisturePercentage(true);
        apiFacility.setLevel(1);
        apiFacility.setTranslations(new ArrayList<>());
        apiFacility.setFacilitySemiProductList(new ArrayList<>());
        apiFacility.setFacilityFinalProducts(new ArrayList<>());
        apiFacility.setFacilityValueChains(new ArrayList<>());

        ApiFacilityType apiFacilityType = new ApiFacilityType();
        apiFacilityType.setId(1L);
        apiFacility.setFacilityType(apiFacilityType);

        ApiFacilityLocation apiLocation = new ApiFacilityLocation();
        ApiAddress apiAddress = new ApiAddress();
        ApiCountry apiCountry = new ApiCountry();
        apiCountry.setId(65L);
        apiAddress.setCountry(apiCountry);
        apiLocation.setAddress(apiAddress);
        apiFacility.setFacilityLocation(apiLocation);

        facilityService.createOrUpdateFacility(apiFacility, userDetails);

        assertTrue(existingFacility.getDisplayFinalPriceDiscount(), "displayFinalPriceDiscount should be persisted as true");
        assertTrue(existingFacility.getDisplayMoisturePercentage(), "displayMoisturePercentage should be persisted as true");
        assertEquals(1, existingFacility.getLevel(), "level should be persisted as 1");
    }
}
