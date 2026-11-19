package com.abelium.inatrace.components.bi;

import com.abelium.inatrace.api.ApiStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Apache Superset BI embedding per tenant environment.
 */
@Component
@ConfigurationProperties(prefix = "inatrace.bi.superset")
public class BiSupersetProperties {

	private String internalUrl = "";
	private String publicDomain = "";
	private String allowedPortalOrigin = "";
	private String organizationSlug = "fortaleza";
	private String environment = "staging";
	private String serviceUsername = "bi_embed_service";
	private String servicePassword = "";
	private int guestTokenTtlSeconds = 300;

	public String getInternalUrl() {
		return internalUrl;
	}

	public void setInternalUrl(String internalUrl) {
		this.internalUrl = internalUrl;
	}

	public String getPublicDomain() {
		return publicDomain;
	}

	public void setPublicDomain(String publicDomain) {
		this.publicDomain = publicDomain;
	}

	public String getAllowedPortalOrigin() {
		return allowedPortalOrigin;
	}

	public void setAllowedPortalOrigin(String allowedPortalOrigin) {
		this.allowedPortalOrigin = allowedPortalOrigin;
	}

	public String getOrganizationSlug() {
		return organizationSlug;
	}

	public void setOrganizationSlug(String organizationSlug) {
		this.organizationSlug = organizationSlug;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getServiceUsername() {
		return serviceUsername;
	}

	public void setServiceUsername(String serviceUsername) {
		this.serviceUsername = serviceUsername;
	}

	public String getServicePassword() {
		return servicePassword;
	}

	public void setServicePassword(String servicePassword) {
		this.servicePassword = servicePassword;
	}

	public int getGuestTokenTtlSeconds() {
		return guestTokenTtlSeconds;
	}

	public void setGuestTokenTtlSeconds(int guestTokenTtlSeconds) {
		this.guestTokenTtlSeconds = guestTokenTtlSeconds;
	}

	public String normalizedInternalUrl() {
		return StringUtils.removeEnd(StringUtils.trimToEmpty(internalUrl), "/");
	}

	public String normalizedPublicDomain() {
		return StringUtils.removeEnd(StringUtils.trimToEmpty(publicDomain), "/");
	}

	public String normalizedPortalOrigin() {
		return StringUtils.removeEnd(StringUtils.trimToEmpty(allowedPortalOrigin), "/");
	}

	public String normalizedOrganizationSlug() {
		return StringUtils.lowerCase(StringUtils.trimToEmpty(organizationSlug));
	}

	public String normalizedEnvironment() {
		return StringUtils.lowerCase(StringUtils.trimToEmpty(environment));
	}

	/**
	 * Validates that Superset gateway credentials and URLs are configured.
	 */
	public void requireConfigured() throws BiReportException {
		if (StringUtils.isAnyBlank(internalUrl, publicDomain, allowedPortalOrigin, serviceUsername, servicePassword)) {
			throw new BiReportException(
					HttpStatus.SERVICE_UNAVAILABLE,
					ApiStatus.UPSTREAM_HTTP_ERROR,
					"El servicio de reportes BI (Superset) no está configurado en el servidor."
			);
		}
	}
}
