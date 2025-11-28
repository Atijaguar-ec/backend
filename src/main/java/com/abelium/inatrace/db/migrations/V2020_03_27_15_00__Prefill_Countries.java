package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.common.Country;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class V2020_03_27_15_00__Prefill_Countries implements JpaMigration 
{
    public void migrate(EntityManager em, Environment environment) throws Exception {
        // Check if countries already exist (idempotent migration)
        Long existingCount = em.createQuery("SELECT COUNT(c) FROM Country c", Long.class).getSingleResult();
        if (existingCount != null && existingCount > 0) {
            // Countries already populated, skip migration
            return;
        }
        
        String path = StringUtils.trim(environment.getProperty("INATrace.import.path"));
        if (path == null || path.isEmpty()) {
            path = "import/";
        }
        
        // Ensure path ends with /
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        
        CSVParser parser = CSVFormat.DEFAULT.
                withDelimiter(',').
                withIgnoreSurroundingSpaces(true).
                withFirstRecordAsHeader().parse(new InputStreamReader(new FileInputStream(path + "countries.csv"), "UTF-8"));
        
        for (CSVRecord rec : parser) {
            String code = rec.get("Code");
            String name = rec.get("Name");
            
            Country c = new Country();
            c.setCode(code);
            c.setName(name);
            em.persist(c);
        }
    }
}
