package com.abelium.inatrace.components.bi;

import com.abelium.inatrace.api.ApiDefaultResponse;
import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.bi.api.ApiBiDashboard;
import com.abelium.inatrace.components.bi.api.ApiBiDashboardCatalogResponse;
import com.abelium.inatrace.components.bi.api.ApiBiGuestTokenRequest;
import com.abelium.inatrace.components.bi.api.ApiBiGuestTokenResponse;
import com.abelium.inatrace.components.bi.api.ApiCustomBiDashboardRequest;
import com.abelium.inatrace.security.annotations.RequireCompanyAccess;
import com.abelium.inatrace.security.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated gateway endpoints for Superset embedded dashboards and admin Studio publishing.
 * PrefixedApiRequestHandler mounts this controller under /api/bi/*.
 */
@RestController
@RequestMapping("/bi")
@Validated
public class BiReportsController {

	private final BiReportsService biReportsService;

	@Autowired
	public BiReportsController(BiReportsService biReportsService) {
		this.biReportsService = biReportsService;
	}

	@GetMapping("/dashboards")
	@PreAuthorize("hasAnyAuthority('USER', 'SYSTEM_ADMIN', 'REGIONAL_ADMIN')")
	@RequireCompanyAccess
	@Operation(summary = "List authorized Superset dashboards for a company")
	public ResponseEntity<ApiResponse<ApiBiDashboardCatalogResponse>> getDashboards(
			@NotNull @RequestParam("companyId") Long companyId,
			@AuthenticationPrincipal CustomUserDetails authUser
	) throws ApiException {
		return noStoreJson(biReportsService.getDashboardCatalog(companyId, authUser));
	}

	@PostMapping("/guest-token")
	@PreAuthorize("hasAnyAuthority('USER', 'SYSTEM_ADMIN', 'REGIONAL_ADMIN')")
	@RequireCompanyAccess
	@Operation(summary = "Issue a short-lived Superset guest token for an authorized dashboard slug")
	public ResponseEntity<ApiResponse<ApiBiGuestTokenResponse>> createGuestToken(
			@Valid @RequestBody ApiBiGuestTokenRequest request,
			@AuthenticationPrincipal CustomUserDetails authUser
	) throws ApiException {
		return noStoreJson(biReportsService.issueGuestToken(request, authUser));
	}

	private static <T> ResponseEntity<ApiResponse<T>> noStoreJson(T payload) {
		return ResponseEntity.ok()
				.cacheControl(CacheControl.noStore().mustRevalidate())
				.header("Pragma", "no-cache")
				.body(new ApiResponse<>(payload));
	}

	@PostMapping("/custom-dashboards")
	@PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'REGIONAL_ADMIN')")
	@RequireCompanyAccess
	@Operation(summary = "Validate and register a Studio-designed Superset dashboard for a company")
	public ApiResponse<ApiBiDashboard> registerCustomDashboard(
			@Valid @RequestBody ApiCustomBiDashboardRequest request,
			@AuthenticationPrincipal CustomUserDetails authUser
	) throws ApiException {
		return new ApiResponse<>(biReportsService.registerCustomDashboard(request, authUser));
	}

	@DeleteMapping("/custom-dashboards/{slug}")
	@PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'REGIONAL_ADMIN')")
	@RequireCompanyAccess
	@Operation(summary = "Unpublish a Studio-designed Superset dashboard from a company")
	public ApiDefaultResponse removeCustomDashboard(
			@NotBlank @PathVariable("slug") String slug,
			@NotNull @RequestParam("companyId") Long companyId,
			@AuthenticationPrincipal CustomUserDetails authUser
	) throws ApiException {
		biReportsService.removeCustomDashboard(companyId, slug, authUser);
		return ApiResponse.ok();
	}
}
