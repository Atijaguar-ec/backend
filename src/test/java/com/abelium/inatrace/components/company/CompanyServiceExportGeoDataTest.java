package com.abelium.inatrace.components.company;

import com.abelium.inatrace.db.entities.common.Address;
import com.abelium.inatrace.db.entities.common.Country;
import com.abelium.inatrace.db.entities.common.Plot;
import com.abelium.inatrace.db.entities.common.PlotCoordinate;
import com.abelium.inatrace.db.entities.common.UserCustomer;
import com.abelium.inatrace.db.entities.common.UserCustomerLocation;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.company.CompanyUser;
import com.abelium.inatrace.db.entities.common.User;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CompanyServiceExportGeoDataTest {

    @Test
    @DisplayName("exportUserCustomerGeoData: maneja fincas sin coordenadas sin arrojar IndexOutOfBoundsException ni NPE")
    void exportUserCustomerGeoData_handlesEmptyCoordinatesSafely() throws Exception {
        CompanyService service = new CompanyService();

        UserCustomer uc = new UserCustomer();
        ReflectionTestUtils.setField(uc, "id", 100L);

        Company company = new Company();
        ReflectionTestUtils.setField(company, "id", 1L);
        CompanyUser cu = new CompanyUser();
        User u = new User();
        ReflectionTestUtils.setField(u, "id", 999L);
        u.setEmail("admin@test.com");
        cu.setUser(u);
        company.setUsers(Set.of(cu));
        uc.setCompany(company);

        // Plot 1: empty coordinates list
        Plot emptyPlot = new Plot();
        ReflectionTestUtils.setField(emptyPlot, "id", 1L);
        emptyPlot.setPlotName("Finca Sin Coordenadas");
        emptyPlot.setCoordinates(new ArrayList<>());

        // Plot 2: null coordinates
        Plot nullCoordPlot = new Plot();
        ReflectionTestUtils.setField(nullCoordPlot, "id", 2L);
        nullCoordPlot.setCoordinates(null);

        // Plot 3: valid point plot
        Plot pointPlot = new Plot();
        ReflectionTestUtils.setField(pointPlot, "id", 3L);
        pointPlot.setPlotName("Finca El Mirador");
        pointPlot.setSize(1.5);
        pointPlot.setGeoId("EC-AGSTACK-103");
        PlotCoordinate pCoord = new PlotCoordinate();
        pCoord.setLongitude(-80.123456);
        pCoord.setLatitude(-0.987654);
        pointPlot.setCoordinates(new ArrayList<>(List.of(pCoord)));

        // Plot 4: valid polygon (open, clockwise)
        Plot polyPlot = new Plot();
        ReflectionTestUtils.setField(polyPlot, "id", 4L);
        polyPlot.setPlotName("Hacienda San Jose");
        polyPlot.setSize(5.2);
        polyPlot.setGeoId("EC-AGSTACK-104");
        PlotCoordinate c1 = new PlotCoordinate(); c1.setLongitude(-80.0); c1.setLatitude(-1.0);
        PlotCoordinate c2 = new PlotCoordinate(); c2.setLongitude(-80.0); c2.setLatitude(-1.1);
        PlotCoordinate c3 = new PlotCoordinate(); c3.setLongitude(-79.9); c3.setLatitude(-1.0);
        polyPlot.setCoordinates(new ArrayList<>(List.of(c1, c2, c3)));

        uc.setPlots(new LinkedHashSet<>(List.of(emptyPlot, nullCoordPlot, pointPlot, polyPlot)));

        // Location & Country
        UserCustomerLocation loc = new UserCustomerLocation();
        Address addr = new Address();
        Country country = new Country();
        country.setCode("ec");
        addr.setCountry(country);
        loc.setAddress(addr);
        uc.setUserCustomerLocation(loc);

        // Mock fetchUserCustomerWithPlots
        CompanyService spyService = org.mockito.Mockito.spy(service);
        org.mockito.Mockito.doReturn(uc).when(spyService).fetchUserCustomerWithPlots(100L);

        CustomUserDetails authUser = new CustomUserDetails(u);

        byte[] geoJsonBytes = spyService.exportUserCustomerGeoData(authUser, 100L);
        assertNotNull(geoJsonBytes);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(geoJsonBytes);
        assertEquals("FeatureCollection", root.get("type").asText());

        JsonNode features = root.get("features");
        assertNotNull(features);
        // Should contain 2 features (pointPlot and polyPlot). emptyPlot and nullCoordPlot are safely omitted.
        assertEquals(2, features.size());

        // Feature 0: Point
        JsonNode f0 = features.get(0);
        assertEquals("Feature", f0.get("type").asText());
        assertEquals("Point", f0.get("geometry").get("type").asText());
        assertEquals("EC", f0.get("properties").get("ProducerCountry").asText());
        assertEquals("Finca El Mirador", f0.get("properties").get("ProductionPlace").asText());
        assertEquals(1.5, f0.get("properties").get("Area").asDouble(), 0.001);

        // Feature 1: Polygon
        JsonNode f1 = features.get(1);
        assertEquals("Feature", f1.get("type").asText());
        assertEquals("Polygon", f1.get("geometry").get("type").asText());
        JsonNode ring = f1.get("geometry").get("coordinates").get(0);
        assertEquals(4, ring.size(), "Polygon ring must be closed with 4 coordinates");
        assertEquals(ring.get(0), ring.get(ring.size() - 1));
        assertEquals("EC", f1.get("properties").get("ProducerCountry").asText());
        assertEquals("Hacienda San Jose", f1.get("properties").get("ProductionPlace").asText());
        assertEquals(5.2, f1.get("properties").get("Area").asDouble(), 0.001);
    }
}
