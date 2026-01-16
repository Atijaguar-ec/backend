package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.MeasureUnitType;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Pece Adjievski, Sunesis d.o.o.
 */
public class V2021_08_11_11_41__Prefill_MeasureUnitTypes implements JpaMigration {

	@Override
	public void migrate(EntityManager em, Environment environment) throws Exception {

		Long count = em.createQuery("SELECT COUNT(mut) FROM MeasureUnitType mut", Long.class).getSingleResult();
		if (count != null && count > 0L) {
			return;
		}

		String productType = environment.getProperty("INATrace.product.type", "COCOA");
		List<MeasureUnitType> measureUnitTypes;

		if ("SHRIMP".equalsIgnoreCase(productType) || "CAMARON".equalsIgnoreCase(productType)) {
			measureUnitTypes = List.of(
					new MeasureUnitType("BINES", "Bines", null),
					new MeasureUnitType("GAVETAS", "Gavetas", null),
					new MeasureUnitType("LIBRAS", "Libras", null));
		} else if ("COCOA".equalsIgnoreCase(productType)) {
			measureUnitTypes = List.of(
					new MeasureUnitType("PESOKG", "kg", new BigDecimal("1")),
					new MeasureUnitType("PESOLIBRA", "Libra", new BigDecimal("2.2")),
					new MeasureUnitType("SACOS_60", "Sacos (60 kg)", new BigDecimal("60")),
					new MeasureUnitType("SACOS_69", "Sacos (69 kg)", new BigDecimal("69")));
		} else {
			measureUnitTypes = List.of(
					new MeasureUnitType("VOLUME_L", "liter", null),
					new MeasureUnitType("WEIGHT_KG", "kg", new BigDecimal("1")),
					new MeasureUnitType("BAG_60", "Bag (60 kg)", new BigDecimal("60")));
		}

		measureUnitTypes.forEach(em::persist);
	}
}
