package com.abelium.inatrace.db.entities.codebook;

import com.abelium.inatrace.db.base.CodebookBaseEntity;
import jakarta.persistence.Entity;

/**
 * Codebook entity for facility types.
 *
 * @author Pece Adjievski, Sunesis d.o.o.
 */
@Entity
public class FacilityType extends CodebookBaseEntity {
  
	@jakarta.persistence.Column(name = "\"order\"")
	private Integer order;

	public FacilityType() {
		super();
	}

	public FacilityType(String code, String label) {
		super(code, label);
	}

	public FacilityType(String code, String label, Integer order) {
		super(code, label);
		this.order = order;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}
}
