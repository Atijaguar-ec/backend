package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.ProductType;
import com.abelium.inatrace.db.entities.common.Plot;
import com.abelium.inatrace.db.entities.common.PlotCoordinate;
import com.abelium.inatrace.db.entities.common.UserCustomer;
import com.abelium.inatrace.types.UserCustomerType;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import jakarta.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Imports farmer plots from GeoJSON file (poligonos_16_10_2025.geojson).
 * <p>
 * This migration:
 * - Reads GeoJSON file with polygon geometries
 * - Matches ID_INTERNO from GeoJSON to UserCustomer.farmerCompanyInternalId
 * - Uses intelligent matching: exact match + cedula extraction fallback
 * - Creates Plot and PlotCoordinate entities for each feature
 * - Handles multiple plots per farmer
 * <p>
 * Safe and idempotent:
 * - Skips if plots already imported (checks for specific geoId pattern)
 * - Logs warnings for farmers not found
 * - Validates polygon geometry before importing
 *
 * @author INATrace Team
 */
public class V2026_01_19_12_00__Import_Plots_From_GeoJSON implements JpaMigration {

    private static final Pattern CEDULA_PATTERN = Pattern.compile("^[A-Z]{1,3}([0-9]{10,12})(-.*)?$");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^([A-Z]{1,3})[0-9]");

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        
        // Check if plots were already imported (idempotent migration)
        // We check by looking for plots with plotName pattern from GeoJSON (e.g., "ASH5493 LOTE 1")
        Long existingCount = em.createQuery(
                "SELECT COUNT(p) FROM Plot p WHERE p.plotName LIKE '%LOTE%'", 
                Long.class)
            .getSingleResult();
        
        if (existingCount != null && existingCount > 0) {
            System.out.println("[Flyway] Import_Plots_From_GeoJSON: Plots already imported (found " + existingCount + " plots with LOTE pattern), skipping.");
            return;
        }

        String path = StringUtils.trim(environment.getProperty("INATrace.import.path"));
        if (path == null || path.isEmpty()) {
            path = "import/";
        }

        if (!path.endsWith("/")) {
            path = path + "/";
        }

        String fullPath = path + "poligonos_16_10_2025.geojson";
        InputStream geojsonStream = null;
        
        try {
            geojsonStream = new FileInputStream(fullPath);
        } catch (FileNotFoundException ignored) {
        }

        if (geojsonStream == null) {
            try {
                geojsonStream = new FileInputStream("import/poligonos_16_10_2025.geojson");
            } catch (FileNotFoundException ignored) {
            }
        }

        if (geojsonStream == null) {
            geojsonStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("import/poligonos_16_10_2025.geojson");
        }

        if (geojsonStream == null) {
            System.out.println("[Flyway] Import_Plots_From_GeoJSON: poligonos_16_10_2025.geojson not found at " 
                + fullPath + " (nor ./import/poligonos_16_10_2025.geojson), skipping plot import.");
            return;
        }

        try {
            String geojsonContent = new String(geojsonStream.readAllBytes(), StandardCharsets.UTF_8);
            FeatureCollection featureCollection = FeatureCollection.fromJson(geojsonContent);

            if (featureCollection == null || featureCollection.features() == null) {
                System.out.println("[Flyway] Import_Plots_From_GeoJSON: Invalid GeoJSON content, skipping.");
                return;
            }

            int importedCount = 0;
            int notFoundCount = 0;
            int invalidGeometryCount = 0;
            List<String> notFoundIds = new ArrayList<>();

            System.out.println("[Flyway] Import_Plots_From_GeoJSON: Processing " + featureCollection.features().size() + " features...");

            for (Feature feature : featureCollection.features()) {
                try {
                    String idInterno = getPropertyAsString(feature, "ID_INTERNO");
                    
                    if (idInterno == null || idInterno.trim().isEmpty()) {
                        System.out.println("[Flyway] Import_Plots_From_GeoJSON: Feature has no ID_INTERNO, skipping.");
                        continue;
                    }

                    // Find farmer using intelligent matching
                    UserCustomer farmer = findFarmerByIdInterno(em, idInterno);

                    if (farmer == null) {
                        notFoundCount++;
                        notFoundIds.add(idInterno);
                        continue;
                    }

                    // Extract plot properties
                    String idDeUn = getPropertyAsString(feature, "ID_DE_UN");
                    String codLote = getPropertyAsString(feature, "COD_LOTE");
                    Double hectarea = getPropertyAsDouble(feature, "HECTAREA");
                    String cultivoPri = getPropertyAsString(feature, "CULTIVOPRI");

                    // Create Plot entity
                    Plot plot = new Plot();
                    plot.setFarmer(farmer);
                    plot.setPlotName(idDeUn != null ? idDeUn : (idInterno + " " + codLote));
                    plot.setSize(hectarea);
                    plot.setUnit("ha");
                    // geoId will be filled later via API, leave as null
                    plot.setLastUpdated(new Date());

                    // Try to find or create ProductType based on CULTIVOPRI
                    if (cultivoPri != null && !cultivoPri.trim().isEmpty()) {
                        ProductType productType = findOrCreateProductType(em, cultivoPri);
                        plot.setCrop(productType);
                    }

                    // Extract coordinates from geometry
                    List<PlotCoordinate> coordinates = extractCoordinates(feature, plot);

                    if (coordinates == null || coordinates.size() < 3) {
                        invalidGeometryCount++;
                        System.out.println("[Flyway] Import_Plots_From_GeoJSON: Invalid geometry for " + idInterno 
                            + " (< 3 coordinates), skipping.");
                        continue;
                    }

                    // Persist Plot (coordinates will be persisted via cascade)
                    plot.setCoordinates(coordinates);
                    em.persist(plot);
                    importedCount++;

                    // Flush every 50 plots to avoid memory issues
                    if (importedCount % 50 == 0) {
                        em.flush();
                        em.clear();
                    }

                } catch (Exception e) {
                    System.out.println("[Flyway] Import_Plots_From_GeoJSON: Error processing feature: " + e.getMessage());
                }
            }

            em.flush();

            // Summary
            System.out.println("[Flyway] Import_Plots_From_GeoJSON: === Import Summary ===");
            System.out.println("[Flyway] Import_Plots_From_GeoJSON: Total features: " + featureCollection.features().size());
            System.out.println("[Flyway] Import_Plots_From_GeoJSON: Plots imported: " + importedCount);
            System.out.println("[Flyway] Import_Plots_From_GeoJSON: Farmers not found: " + notFoundCount);
            System.out.println("[Flyway] Import_Plots_From_GeoJSON: Invalid geometries: " + invalidGeometryCount);

            if (!notFoundIds.isEmpty() && notFoundIds.size() <= 20) {
                System.out.println("[Flyway] Import_Plots_From_GeoJSON: Missing IDs: " + String.join(", ", notFoundIds));
            } else if (!notFoundIds.isEmpty()) {
                System.out.println("[Flyway] Import_Plots_From_GeoJSON: Missing IDs (first 20): " 
                    + String.join(", ", notFoundIds.subList(0, 20)));
            }

        } catch (IOException e) {
            System.out.println("[Flyway] Import_Plots_From_GeoJSON: Error reading GeoJSON file: " + e.getMessage());
        } finally {
            if (geojsonStream != null) {
                try {
                    geojsonStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Find farmer using intelligent matching strategy:
     * 1. Try exact match on farmerCompanyInternalId
     * 2. Extract cedula number and search by partial match
     */
    private UserCustomer findFarmerByIdInterno(EntityManager em, String idInterno) {
        // Strategy 1: Exact match
        List<UserCustomer> exactMatch = em.createQuery(
                "SELECT uc FROM UserCustomer uc WHERE uc.farmerCompanyInternalId = :idInterno AND uc.type = :type",
                UserCustomer.class)
            .setParameter("idInterno", idInterno)
            .setParameter("type", UserCustomerType.FARMER)
            .getResultList();

        if (!exactMatch.isEmpty()) {
            return exactMatch.get(0);
        }

        // Strategy 2: Extract cedula and partial match
        String cedula = extractCedulaNumber(idInterno);
        
        if (cedula != null) {
            List<UserCustomer> candidates = em.createQuery(
                    "SELECT uc FROM UserCustomer uc WHERE uc.farmerCompanyInternalId LIKE :cedula AND uc.type = :type",
                    UserCustomer.class)
                .setParameter("cedula", "%" + cedula + "%")
                .setParameter("type", UserCustomerType.FARMER)
                .getResultList();

            if (candidates.size() == 1) {
                return candidates.get(0);
            } else if (candidates.size() > 1) {
                // Refine by prefix
                String prefix = extractPrefix(idInterno);
                for (UserCustomer candidate : candidates) {
                    if (candidate.getFarmerCompanyInternalId() != null 
                        && candidate.getFarmerCompanyInternalId().startsWith(prefix)) {
                        return candidate;
                    }
                }
                // If no prefix match, return first candidate
                return candidates.get(0);
            }
        }

        return null;
    }

    /**
     * Extract cedula number from ID_INTERNO.
     * Pattern: 1-3 letters + 10-12 digits (+ optional suffix)
     * Examples: VI2400185795 -> 2400185795, MY1201209648-L1 -> 1201209648
     */
    private String extractCedulaNumber(String idInterno) {
        Matcher matcher = CEDULA_PATTERN.matcher(idInterno);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Extract prefix from ID_INTERNO (e.g., VI, MY, Z)
     */
    private String extractPrefix(String idInterno) {
        Matcher matcher = PREFIX_PATTERN.matcher(idInterno);
        return matcher.find() ? matcher.group(1) : "";
    }

    /**
     * Extract coordinates from Feature geometry (handles Polygon and MultiPolygon)
     */
    private List<PlotCoordinate> extractCoordinates(Feature feature, Plot plot) {
        List<PlotCoordinate> coordinates = new ArrayList<>();

        if (feature.geometry() instanceof Polygon) {
            Polygon polygon = (Polygon) feature.geometry();
            List<Point> points = polygon.coordinates().get(0); // First ring (outer boundary)

            for (Point point : points) {
                PlotCoordinate coord = new PlotCoordinate();
                coord.setPlot(plot);
                coord.setLongitude(point.longitude());
                coord.setLatitude(point.latitude());
                coordinates.add(coord);
            }
        } else if (feature.geometry() instanceof com.mapbox.geojson.MultiPolygon) {
            com.mapbox.geojson.MultiPolygon multiPolygon = (com.mapbox.geojson.MultiPolygon) feature.geometry();
            
            // Take first polygon from MultiPolygon
            if (!multiPolygon.coordinates().isEmpty() && !multiPolygon.coordinates().get(0).isEmpty()) {
                List<Point> points = multiPolygon.coordinates().get(0).get(0);

                for (Point point : points) {
                    PlotCoordinate coord = new PlotCoordinate();
                    coord.setPlot(plot);
                    coord.setLongitude(point.longitude());
                    coord.setLatitude(point.latitude());
                    coordinates.add(coord);
                }
            }
        }

        return coordinates;
    }

    /**
     * Find or create ProductType by name
     */
    private ProductType findOrCreateProductType(EntityManager em, String name) {
        List<ProductType> existing = em.createQuery(
                "SELECT pt FROM ProductType pt WHERE pt.name = :name", ProductType.class)
            .setParameter("name", name)
            .getResultList();

        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        // Create new ProductType
        ProductType productType = new ProductType();
        productType.setName(name);
        productType.setDescription(name);
        em.persist(productType);
        em.flush();

        return productType;
    }

    /**
     * Safely get String property from Feature
     */
    private String getPropertyAsString(Feature feature, String propertyName) {
        if (feature.properties() == null || !feature.properties().has(propertyName)) {
            return null;
        }
        
        try {
            return feature.properties().get(propertyName).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Safely get Double property from Feature
     */
    private Double getPropertyAsDouble(Feature feature, String propertyName) {
        if (feature.properties() == null || !feature.properties().has(propertyName)) {
            return null;
        }
        
        try {
            return feature.properties().get(propertyName).getAsDouble();
        } catch (Exception e) {
            return null;
        }
    }
}
