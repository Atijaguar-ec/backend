package com.abelium.inatrace.db.entities.facility;

import com.abelium.inatrace.db.base.TimestampEntity;
import com.abelium.inatrace.db.entities.codebook.FacilityType;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import com.abelium.inatrace.db.entities.value_chain.FacilityValueChain;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table
@NamedQueries({
	@NamedQuery(name = "Facility.listCollectingFacilitiesByCompany",
				query = "SELECT f FROM Facility f INNER JOIN FETCH f.facilityTranslations t INNER JOIN f.company c WHERE c.id = :companyId AND f.isCollectionFacility = true AND t.language = :language AND (f.isDeactivated IS NULL OR f.isDeactivated = false)"),
	@NamedQuery(name = "Facility.countCollectingFacilitiesByCompany",
				query = "SELECT COUNT(f) FROM Facility f WHERE f.company.id = :companyId AND f.isCollectionFacility = true AND (f.isDeactivated IS NULL OR f.isDeactivated = false)"),
	@NamedQuery(name = "Facility.countCompanyFacilities",
			    query = "SELECT COUNT(f) FROM Facility f WHERE f.company.id = :companyId")
})
public class Facility extends TimestampEntity {

	@Version
	private Long entityVersion;

	@Column
	private String name;

	@Column
	private Boolean isCollectionFacility;

	@Column
	private Boolean isPublic;

	@Column
	private Boolean displayMayInvolveCollectors;

	@Column
	private Boolean displayOrganic;

	@Column
	private Boolean displayPriceDeductionDamage;

	@Column
	private Boolean displayFinalPriceDiscount;

	@Column
	private Boolean displayWeightDeductionDamage;

	@Column
	private Boolean displayMoisturePercentage;

	@Column
	private Boolean displayTare;

	@Column
	private Boolean displayWomenOnly;

	@Column
	private Boolean isDeactivated;

	@Column
	private Boolean displayPriceDeterminedLater;

	@Column(name = "isFieldInspection")
	private Boolean isFieldInspection;

	@Column(name = "isLaboratory")
	private Boolean isLaboratory;

	@Column(name = "isClassificationProcess")
	private Boolean isClassificationProcess;

	@Column(name = "isFreezingProcess")
	private Boolean isFreezingProcess;

	@Column(name = "isCuttingProcess")
	private Boolean isCuttingProcess;

	@Column(name = "isTreatmentProcess")
	private Boolean isTreatmentProcess;

	@Column(name = "isTunnelFreezing")
	private Boolean isTunnelFreezing;

	@Column(name = "isWashingArea")
	private Boolean isWashingArea;

	@Column(name = "isRestArea")
	private Boolean isRestArea;

	@Column(name = "isDeheadingProcess")
	private Boolean isDeheadingProcess;
	@Column
	private Integer level;

	@OneToOne(cascade = CascadeType.ALL)
	private FacilityLocation facilityLocation;

	@ManyToOne
	private Company company;

	@ManyToOne(cascade = CascadeType.ALL)
	private FacilityType facilityType;

	@OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<FacilitySemiProduct> facilitySemiProducts;

	@OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<FacilityFinalProduct> facilityFinalProducts;

	@OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<FacilityValueChain> facilityValueChains;
	
	@OneToMany(mappedBy = "facility", cascade = CascadeType.ALL)
	private Set<StockOrder> stockOrders;

	@OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<FacilityTranslation> facilityTranslations;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getIsCollectionFacility() {
		return isCollectionFacility;
	}

	public void setIsCollectionFacility(Boolean isCollectionFacility) {
		this.isCollectionFacility = isCollectionFacility;
	}

	public Boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	public Boolean getDisplayMayInvolveCollectors() {
		return displayMayInvolveCollectors;
	}

	public void setDisplayMayInvolveCollectors(Boolean displayMayInvolveCollectors) {
		this.displayMayInvolveCollectors = displayMayInvolveCollectors;
	}

	public Boolean getDisplayOrganic() {
		return displayOrganic;
	}

	public void setDisplayOrganic(Boolean displayOrganic) {
		this.displayOrganic = displayOrganic;
	}

	public Boolean getDisplayPriceDeductionDamage() {
		return displayPriceDeductionDamage;
	}

	public void setDisplayPriceDeductionDamage(Boolean displayPriceDeductionDamage) {
		this.displayPriceDeductionDamage = displayPriceDeductionDamage;
	}

	public Boolean getDisplayFinalPriceDiscount() {
		return displayFinalPriceDiscount;
	}

	public void setDisplayFinalPriceDiscount(Boolean displayFinalPriceDiscount) {
		this.displayFinalPriceDiscount = displayFinalPriceDiscount;
	}

	public Boolean getDisplayWeightDeductionDamage() {
		return displayWeightDeductionDamage;
	}

	public void setDisplayWeightDeductionDamage(Boolean displayWeightDeductionDamage) {
		this.displayWeightDeductionDamage = displayWeightDeductionDamage;
	}

	public Boolean getDisplayMoisturePercentage() {
		return displayMoisturePercentage;
	}

	public void setDisplayMoisturePercentage(Boolean displayMoisturePercentage) {
		this.displayMoisturePercentage = displayMoisturePercentage;
	}

	public Boolean getDisplayTare() {
		return displayTare;
	}

	public void setDisplayTare(Boolean displayTare) {
		this.displayTare = displayTare;
	}

	public Boolean getDisplayWomenOnly() {
		return displayWomenOnly;
	}

	public void setDisplayWomenOnly(Boolean displayWomenOnly) {
		this.displayWomenOnly = displayWomenOnly;
	}

	public Boolean getIsDeactivated() {
		return isDeactivated;
	}

	public void setIsDeactivated(Boolean deactivated) {
		isDeactivated = deactivated;
	}

	public Boolean getDisplayPriceDeterminedLater() {
		return displayPriceDeterminedLater;
	}

	public void setDisplayPriceDeterminedLater(Boolean priceDeterminedLater) {
		this.displayPriceDeterminedLater = priceDeterminedLater;
	}

	public Boolean getIsFieldInspection() {
		return isFieldInspection;
	}

	public void setIsFieldInspection(Boolean isFieldInspection) {
		this.isFieldInspection = isFieldInspection;
	}

	public Boolean getIsLaboratory() {
		return isLaboratory;
	}

	public void setIsLaboratory(Boolean isLaboratory) {
		this.isLaboratory = isLaboratory;
	}

	public Boolean getIsClassificationProcess() {
		return isClassificationProcess;
	}

	public void setIsClassificationProcess(Boolean isClassificationProcess) {
		this.isClassificationProcess = isClassificationProcess;
	}

	public Boolean getIsFreezingProcess() {
		return isFreezingProcess;
	}

	public void setIsFreezingProcess(Boolean isFreezingProcess) {
		this.isFreezingProcess = isFreezingProcess;
	}

	public Boolean getIsCuttingProcess() {
		return isCuttingProcess;
	}

	public void setIsCuttingProcess(Boolean isCuttingProcess) {
		this.isCuttingProcess = isCuttingProcess;
	}

	public Boolean getIsTreatmentProcess() {
		return isTreatmentProcess;
	}

	public void setIsTreatmentProcess(Boolean isTreatmentProcess) {
		this.isTreatmentProcess = isTreatmentProcess;
	}

	public Boolean getIsTunnelFreezing() {
		return isTunnelFreezing;
	}

	public void setIsTunnelFreezing(Boolean isTunnelFreezing) {
		this.isTunnelFreezing = isTunnelFreezing;
	}

	public Boolean getIsWashingArea() {
		return isWashingArea;
	}

	public void setIsWashingArea(Boolean isWashingArea) {
		this.isWashingArea = isWashingArea;
	}

	public Boolean getIsRestArea() {
		return isRestArea;
	}

	public void setIsRestArea(Boolean isRestArea) {
		this.isRestArea = isRestArea;
	}

	public Boolean getIsDeheadingProcess() {
		return isDeheadingProcess;
	}

	public void setIsDeheadingProcess(Boolean isDeheadingProcess) {
		this.isDeheadingProcess = isDeheadingProcess;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public FacilityLocation getFacilityLocation() {
		return facilityLocation;
	}

	public void setFacilityLocation(FacilityLocation facilityLocation) {
		this.facilityLocation = facilityLocation;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public FacilityType getFacilityType() {
		return facilityType;
	}

	public void setFacilityType(FacilityType facilityType) {
		this.facilityType = facilityType;
	}

	public Set<FacilitySemiProduct> getFacilitySemiProducts() {
		if (facilitySemiProducts == null) {
			facilitySemiProducts = new HashSet<>();
		}
		return facilitySemiProducts;
	}

	public Set<FacilityFinalProduct> getFacilityFinalProducts() {
		if (facilityFinalProducts == null) {
			facilityFinalProducts = new HashSet<>();
		}
		return facilityFinalProducts;
	}

	public Set<StockOrder> getStockOrders() {
		return stockOrders;
	}

	public Set<FacilityValueChain> getFacilityValueChains() {
		if (facilityValueChains == null) {
			facilityValueChains = new HashSet<>();
		}
		return facilityValueChains;
	}

	public void setFacilityValueChains(Set<FacilityValueChain> facilityValueChains) {
		this.facilityValueChains = facilityValueChains;
	}

	public void setStockOrders(Set<StockOrder> stockOrders) {
		this.stockOrders = stockOrders;
	}

	public Set<FacilityTranslation> getFacilityTranslations() {
		if (facilityTranslations == null) {
			facilityTranslations = new HashSet<>();
		}
		return facilityTranslations;
	}

	public Facility() {
		super();
	}

	public Facility(String name, Boolean isCollectionFacility, Boolean isPublic, Boolean isDeactivated,
			FacilityLocation facilityLocation, Company company, FacilityType facilityType,
			Set<FacilitySemiProduct> facilitySemiProducts, Set<StockOrder> stockOrders, Set<FacilityTranslation> facilityTranslations) {
		super();
		this.name = name;
		this.isCollectionFacility = isCollectionFacility;
		this.isPublic = isPublic;
		this.isDeactivated = isDeactivated;
		this.facilityLocation = facilityLocation;
		this.company = company;
		this.facilityType = facilityType;
		this.facilitySemiProducts = facilitySemiProducts;
		this.stockOrders = stockOrders;
		this.facilityTranslations = facilityTranslations;
	}

}
