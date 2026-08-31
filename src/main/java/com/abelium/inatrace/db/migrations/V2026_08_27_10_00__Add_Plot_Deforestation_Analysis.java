package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import jakarta.persistence.EntityManager;
import org.springframework.core.env.Environment;

/**
 * Creates the table that stores the Whisp deforestation analyses of a plot.
 *
 * Written as the record of intent and for clean installations, but the design does not
 * depend on it running: section 12.4 of agent-context.md documents installations where no
 * JpaMigration ever executed. Every column is nullable, so hbm2ddl creates the table from
 * the entity mapping on its own; what this migration adds on top is the index on
 * plot_id, which the "latest analysis of this plot" lookup uses on every plot screen.
 */
public class V2026_08_27_10_00__Add_Plot_Deforestation_Analysis implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {

        em.createNativeQuery(
                "CREATE TABLE IF NOT EXISTS plotdeforestationanalysis (" +
                "  id BIGSERIAL PRIMARY KEY," +
                "  plot_id BIGINT," +
                "  status VARCHAR(40)," +
                "  whisptoken VARCHAR(255)," +
                "  submissionmode VARCHAR(40)," +
                "  geoid VARCHAR(255)," +
                "  requestedat TIMESTAMP," +
                "  completedat TIMESTAMP," +
                "  progresspercent INT," +
                "  riskpcrop VARCHAR(255)," +
                "  indicatortreecover VARCHAR(255)," +
                "  indicatorcommodities VARCHAR(255)," +
                "  indicatordisturbancebefore2020 VARCHAR(255)," +
                "  indicatordisturbanceafter2020 VARCHAR(255)," +
                "  area DOUBLE PRECISION," +
                "  areaunit VARCHAR(40)," +
                "  country VARCHAR(255)," +
                "  resultjson TEXT," +
                "  errormessage TEXT" +
                ")").executeUpdate();

        em.createNativeQuery(
                "CREATE INDEX IF NOT EXISTS idx_plotdeforestationanalysis_plot " +
                "ON plotdeforestationanalysis (plot_id)").executeUpdate();
    }

}
