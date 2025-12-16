package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.FacilityType;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * @author Pece Adjievski, Sunesis d.o.o.
 */
public class V2021_08_11_11_33__Prefill_FacilityTypes implements JpaMigration {

	@Override
	public void migrate(EntityManager em, Environment environment) throws Exception {

		Long count = em.createQuery("SELECT COUNT(ft) FROM FacilityType ft", Long.class).getSingleResult();
		if (count != null && count > 0L) {
			return;
		}

		String productType = environment.getProperty("INATrace.product.type", "COFFEE");
		List<FacilityType> facilityTypes;

		if ("COCOA".equalsIgnoreCase(productType)) {
			facilityTypes = List.of(
					new FacilityType("ACOPIO", "Collection Center", 1),
					new FacilityType("ESCURRIDO", "Draining", 2),
					new FacilityType("FERMENTACION", "Fermentation Area", 3),
					new FacilityType("SECADO", "Drying Area", 4),
					new FacilityType("SECADON", "Natural Drying", 5),
					new FacilityType("SECADOA", "Artificial Drying", 6),
					new FacilityType("ALIMPIEZA", "Cleaning Area", 7),
					new FacilityType("ACLASIFICADO", "Grading Area", 8),
					new FacilityType("AEMPACADO", "Packing Area", 9),
					new FacilityType("ALMACEN", "Storage Area", 10),
					new FacilityType("VENTA", "Point of Sale", 11));
		} else {
			facilityTypes = List.of(
					new FacilityType("WASHING_STATION", "Washing station", 1),
					new FacilityType("DRYING_BED", "Drying bed", 2),
					new FacilityType("HULLING_STATION", "Hulling station", 3),
					new FacilityType("STORAGE", "Storage", 4));
		}

		facilityTypes.forEach(em::persist);
	}
}
