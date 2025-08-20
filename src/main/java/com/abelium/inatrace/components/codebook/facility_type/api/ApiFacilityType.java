package com.abelium.inatrace.components.codebook.facility_type.api;

import com.abelium.inatrace.api.ApiCodebookBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

/**
 * Facility type API model.
 *
 * @author Pece Adjievski, Sunesis d.o.o.
 */
@Validated
public class ApiFacilityType extends ApiCodebookBaseEntity {

	@Schema(description = "Order for displaying facility types (lower = first)")
	private Integer order;

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}
}
