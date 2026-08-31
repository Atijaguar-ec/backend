package com.abelium.inatrace.components.whisp;

import com.abelium.inatrace.api.ApiResponse;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.whisp.api.ApiWhispIntegrationStatus;
import com.abelium.inatrace.components.whisp.api.ApiWhispPlotAnalysis;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Deforestation risk analysis of farmer plots through the Whisp API.
 */
@RestController
@RequestMapping("/whisp")
public class WhispController {

	private final WhispAnalysisService whispAnalysisService;

	public WhispController(WhispAnalysisService whispAnalysisService) {
		this.whispAnalysisService = whispAnalysisService;
	}

	@PostMapping(value = "/plots/{plotId}/analysis")
	@Operation(summary = "Submit a plot to Whisp for deforestation analysis. "
			+ "Returns the queued job; poll the GET endpoint for the result.")
	public ApiResponse<ApiWhispPlotAnalysis> requestPlotAnalysis(
			@AuthenticationPrincipal CustomUserDetails authUser,
			@Valid @Parameter(description = "Plot ID", required = true) @PathVariable("plotId") Long plotId) throws ApiException {
		return new ApiResponse<>(whispAnalysisService.requestAnalysis(plotId, authUser));
	}

	@GetMapping(value = "/plots/{plotId}/analysis")
	@Operation(summary = "Get the latest deforestation analysis of a plot, refreshing it from Whisp "
			+ "while the job is still running. Empty when the plot has never been analysed.")
	public ApiResponse<ApiWhispPlotAnalysis> getPlotAnalysis(
			@AuthenticationPrincipal CustomUserDetails authUser,
			@Valid @Parameter(description = "Plot ID", required = true) @PathVariable("plotId") Long plotId) throws ApiException {
		return new ApiResponse<>(whispAnalysisService.getAnalysis(plotId, authUser));
	}

	@GetMapping(value = "/schemas")
	@Operation(summary = "Get the Whisp analysis schemas: the datasets and result fields an analysis can report")
	public ApiResponse<JsonNode> getAnalysisSchemas() throws ApiException {
		return new ApiResponse<>(whispAnalysisService.getAnalysisSchemas());
	}

	@GetMapping(value = "/status")
	@Operation(summary = "Check whether the Whisp integration is configured and reachable")
	public ApiResponse<ApiWhispIntegrationStatus> getIntegrationStatus() {
		return new ApiResponse<>(whispAnalysisService.getIntegrationStatus());
	}

}
