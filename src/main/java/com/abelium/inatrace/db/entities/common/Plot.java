package com.abelium.inatrace.db.entities.common;

import com.abelium.inatrace.api.types.Lengths;
import com.abelium.inatrace.db.base.BaseEntity;
import com.abelium.inatrace.db.entities.codebook.CertificationType;
import com.abelium.inatrace.db.entities.codebook.ProductType;
import com.abelium.inatrace.types.CocoaVariety;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Entity representing farmer plot. Used only for user customer of type FARMER.
 *
 * @author Pece Adjievski, Sunesis d.o.o.
 */
@Entity
public class Plot extends BaseEntity {

	@Column
	private String plotName;

	@ManyToOne
	private ProductType crop;

	@Column
	private Integer numberOfPlants;

	@Column
	private String unit;

	@Column
	private Double size;

	@Column
	private String geoId;

	/**
	 * Production estimate for the plot. A plain @Column on a BigDecimal maps to
	 * numeric(38,2) here, matching the two decimal places asked for.
	 */
	@Column
	private BigDecimal productionEstimate;

	/**
	 * Certification scheme of the plot.
	 *
	 * Apunta al MISMO catálogo administrable (Ajustes → Tipos de certificación) que
	 * alimenta el campo "Tipo de certificación" del formulario de Recepción. No es un
	 * enum: si lo fuera, renombrar un valor en Ajustes dejaría a las parcelas con un
	 * vocabulario propio y divergente.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "certificationtype_id")
	private CertificationType certificationType;

	@Enumerated(EnumType.STRING)
	@Column(length = Lengths.ENUM)
	private CocoaVariety cocoaVariety;

	@Column
	private Date organicStartOfTransition;

	@Column
	private Date lastUpdated;

	@OneToMany(mappedBy = "plot",
	           cascade = CascadeType.ALL,
	           orphanRemoval = true,
	           fetch = FetchType.LAZY)
	private List<PlotCoordinate> coordinates;

	@ManyToOne
	private UserCustomer farmer;

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	public ProductType getCrop() {
		return crop;
	}

	public void setCrop(ProductType crop) {
		this.crop = crop;
	}

	public Integer getNumberOfPlants() {
		return numberOfPlants;
	}

	public void setNumberOfPlants(Integer numberOfPlants) {
		this.numberOfPlants = numberOfPlants;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Double getSize() {
		return size;
	}

	public void setSize(Double size) {
		this.size = size;
	}

	public String getGeoId() {
		return geoId;
	}

	public void setGeoId(String geoId) {
		this.geoId = geoId;
	}

	public BigDecimal getProductionEstimate() {
		return productionEstimate;
	}

	public void setProductionEstimate(BigDecimal productionEstimate) {
		this.productionEstimate = productionEstimate;
	}

	public CertificationType getCertificationType() {
		return certificationType;
	}

	public void setCertificationType(CertificationType certificationType) {
		this.certificationType = certificationType;
	}

	public CocoaVariety getCocoaVariety() {
		return cocoaVariety;
	}

	public void setCocoaVariety(CocoaVariety cocoaVariety) {
		this.cocoaVariety = cocoaVariety;
	}

	public Date getOrganicStartOfTransition() {
		return organicStartOfTransition;
	}

	public void setOrganicStartOfTransition(Date organicStartOfTransition) {
		this.organicStartOfTransition = organicStartOfTransition;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public List<PlotCoordinate> getCoordinates() {
		if (coordinates == null) {
			coordinates = new ArrayList<>();
		}
		return coordinates;
	}

	public void setCoordinates(List<PlotCoordinate> coordinates) {
		this.coordinates = coordinates;
	}

	public UserCustomer getFarmer() {
		return farmer;
	}

	public void setFarmer(UserCustomer farmer) {
		this.farmer = farmer;
	}

}
