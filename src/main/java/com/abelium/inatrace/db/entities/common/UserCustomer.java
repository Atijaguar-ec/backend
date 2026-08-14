package com.abelium.inatrace.db.entities.common;

import com.abelium.inatrace.api.types.Lengths;
import com.abelium.inatrace.db.base.BaseEntity;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.product.Product;
import com.abelium.inatrace.types.Gender;
import com.abelium.inatrace.types.PersonType;
import com.abelium.inatrace.types.UserCustomerStatus;
import com.abelium.inatrace.types.UserCustomerType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@NamedQueries({
	@NamedQuery(name = "UserCustomer.getUserCustomerByCompanyIdAndType",
			    query = "SELECT u FROM UserCustomer u WHERE u.company.id = :companyId AND u.type = :type"),
	@NamedQuery(name = "UserCustomer.getUserCustomerByNameSurnameAndVillage",
			    query = "SELECT u FROM UserCustomer u WHERE u.name = :name AND u.surname = :surname AND u.userCustomerLocation.address.village = :village"),
	@NamedQuery(name = "UserCustomer.countCompanyFarmers",
			    query = "SELECT COUNT(u) FROM UserCustomer u WHERE u.company.id = :companyId AND u.type = com.abelium.inatrace.types.UserCustomerType.FARMER")
})
public class UserCustomer extends BaseEntity {

	/**
	 * product this collector belongs to
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	private Product product;

	/**
	 * company reference
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	private Company company;

	/**
	 * company farmer internal id
	 */
	@Column(length = Lengths.DEFAULT)
	private String farmerCompanyInternalId;
	
	/**
	 * type (collector, farmer)
	 */
	@Enumerated(EnumType.STRING)
	@Column(length = Lengths.ENUM)
	private UserCustomerType type;

	/**
	 * status within the organization (active, suspended, retired).
	 *
	 * Deliberately nullable at the DB level, even though every row is conceptually
	 * ACTIVE or something else: Hibernate's own hbm2ddl.auto=update runs BEFORE our
	 * Flyway-based JpaMigration classes (it bootstraps as part of building the
	 * EntityManagerFactory that the migration step depends on), so a NOT NULL column
	 * added here would race the backfill and fail outright against a table that
	 * already has rows - confirmed live on the UNOCACE staging DB. getStatus() below
	 * treats a null column as ACTIVE, and userCustomerListQueryObject() applies the
	 * same rule to the SQL filter, so the DB-level nullability never leaks out as a
	 * behavioral difference.
	 */
	@Enumerated(EnumType.STRING)
	@Column(length = Lengths.ENUM)
	private UserCustomerStatus status = UserCustomerStatus.ACTIVE;

	/**
	 * reason given for the last status change (why the farmer was suspended or retired)
	 */
	@Column(length = Lengths.DEFAULT)
	private String statusReason;

	/**
	 * when the status was last changed
	 */
	@Column
	private Instant statusUpdateTimestamp;

	/**
	 * user that performed the last status change
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "statusupdatedby_id")
	private User statusUpdatedBy;

	/**
	 * name
	 */
	@Column(length = Lengths.NAME)
	private String name;

	/**
	 * surname
	 */
	@Column(length = Lengths.SURNAME)
	private String surname;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private PersonType personType;

	@Column(length = 255)
	private String companyName;

	@Column(length = 255)
	private String legalRepresentative;
	
	/**
	 * phone number
	 */
	@Column(length = Lengths.PHONE_NUMBER)
	private String phone;
	
	/**
	 * email
	 */
	@Column(length = Lengths.EMAIL)
	private String email;

	@Column
	private Boolean hasSmartphone;

	/**
	 * location (text description)
	 */
	@Column(length = Lengths.DEFAULT)
	private String location;

	@OneToOne
	private UserCustomerLocation userCustomerLocation;
	
	/**
	 * gender
	 */
	@Enumerated(EnumType.STRING)
	@Column(length = Lengths.ENUM)
	private Gender gender;

	@Column
	private BankInformation bank;

	@Column
	private FarmInformation farm;

	@OneToMany(mappedBy = "userCustomer", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<UserCustomerAssociation> associations;

	@OneToMany(mappedBy = "userCustomer", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<UserCustomerCooperative> cooperatives;

	@OneToMany(mappedBy = "userCustomer", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<UserCustomerCertification> certifications;

	@OneToMany(mappedBy = "userCustomer", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<UserCustomerProductType> productTypes;

	@OneToMany(mappedBy = "userCustomer", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<FarmPlantInformation> farmPlantInformationList;

	@OneToMany(mappedBy = "farmer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<Plot> plots;
	
	public UserCustomerType getType() {
		return type;
	}

	public void setType(UserCustomerType type) {
		this.type = type;
	}

	public UserCustomerStatus getStatus() {
		return status != null ? status : UserCustomerStatus.ACTIVE;
	}

	public void setStatus(UserCustomerStatus status) {
		this.status = status;
	}

	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}

	public Instant getStatusUpdateTimestamp() {
		return statusUpdateTimestamp;
	}

	public void setStatusUpdateTimestamp(Instant statusUpdateTimestamp) {
		this.statusUpdateTimestamp = statusUpdateTimestamp;
	}

	public User getStatusUpdatedBy() {
		return statusUpdatedBy;
	}

	public void setStatusUpdatedBy(User statusUpdatedBy) {
		this.statusUpdatedBy = statusUpdatedBy;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public String getFarmerCompanyInternalId() {
		return farmerCompanyInternalId;
	}

	public void setFarmerCompanyInternalId(String farmerCompanyInternalId) {
		this.farmerCompanyInternalId = farmerCompanyInternalId;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public PersonType getPersonType() {
		return personType;
	}

	public void setPersonType(PersonType personType) {
		this.personType = personType;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getLegalRepresentative() {
		return legalRepresentative;
	}

	public void setLegalRepresentative(String legalRepresentative) {
		this.legalRepresentative = legalRepresentative;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getHasSmartphone() {
		return hasSmartphone;
	}

	public void setHasSmartphone(Boolean hasSmartphone) {
		this.hasSmartphone = hasSmartphone;
	}

	public UserCustomerLocation getUserCustomerLocation() {
		return userCustomerLocation;
	}

	public void setUserCustomerLocation(UserCustomerLocation userCustomerLocation) {
		this.userCustomerLocation = userCustomerLocation;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public BankInformation getBank() {
		return bank;
	}

	public void setBank(BankInformation bank) {
		this.bank = bank;
	}

	public FarmInformation getFarm() {
		return farm;
	}

	public void setFarm(FarmInformation farm) {
		this.farm = farm;
	}

	public Set<UserCustomerAssociation> getAssociations() {
		return associations;
	}

	public void setAssociations(Set<UserCustomerAssociation> associations) {
		this.associations = associations;
	}

	public Set<UserCustomerCooperative> getCooperatives() {
		return cooperatives;
	}

	public void setCooperatives(Set<UserCustomerCooperative> cooperatives) {
		this.cooperatives = cooperatives;
	}

	public Set<UserCustomerCertification> getCertifications() {
		if (certifications == null) {
			certifications = new HashSet<>();
		}
		return certifications;
	}

	public void setCertifications(Set<UserCustomerCertification> certifications) {
		this.certifications = certifications;
	}

	public Set<UserCustomerProductType> getProductTypes() {
		if (productTypes == null) {
			productTypes = new HashSet<>();
		}
		return productTypes;
	}

	public void setProductTypes(Set<UserCustomerProductType> productTypes) {
		this.productTypes = productTypes;
	}

	public Set<FarmPlantInformation> getFarmPlantInformationList() {
		if (farmPlantInformationList == null) {
			farmPlantInformationList = new HashSet<>();
		}
		return farmPlantInformationList;
	}

	public void setFarmPlantInformationList(Set<FarmPlantInformation> farmPlantInformationList) {
		this.farmPlantInformationList = farmPlantInformationList;
	}

	public Set<Plot> getPlots() {
		if (plots == null) {
			plots = new HashSet<>();
		}
		return plots;
	}

	public void setPlots(Set<Plot> plots) {
		this.plots = plots;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
