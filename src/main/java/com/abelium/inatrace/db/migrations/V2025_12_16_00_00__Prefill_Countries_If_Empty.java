package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.common.Country;
import jakarta.persistence.EntityManager;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class V2025_12_16_00_00__Prefill_Countries_If_Empty implements JpaMigration {
    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        Long existingCount = em.createQuery("SELECT COUNT(c) FROM Country c", Long.class).getSingleResult();
        if (existingCount != null && existingCount > 0) {
            return;
        }

        String path = StringUtils.trim(environment.getProperty("INATrace.import.path"));
        if (path == null || path.isEmpty()) {
            path = "import/";
        }

        if (!path.endsWith("/")) {
            path = path + "/";
        }

        String fullPath = path + "countries.csv";
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(fullPath), StandardCharsets.UTF_8)) {
            CSVParser parser = CSVFormat.DEFAULT.
                    withDelimiter(',').
                    withIgnoreSurroundingSpaces(true).
                    withFirstRecordAsHeader().parse(reader);

            for (CSVRecord rec : parser) {
                String code = rec.get("Code");
                String name = rec.get("Name");

                Country c = new Country();
                c.setCode(code);
                c.setName(name);
                em.persist(c);
            }
        } catch (FileNotFoundException e) {
            System.out.println("[Flyway] Prefill_Countries_If_Empty: countries.csv not found at " + fullPath + ", skipping country prefill.");
        }
    }
}
