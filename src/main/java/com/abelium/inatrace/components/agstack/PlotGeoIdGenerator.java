package com.abelium.inatrace.components.agstack;

import com.abelium.inatrace.components.agstack.api.ApiRegisterFieldBoundaryResponse;
import com.abelium.inatrace.db.entities.common.PlotCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers a plot boundary in the AgStack asset registry and returns its geo id.
 *
 * Shared by every path that creates plots (the farmer form, the per-farmer GeoJSON upload
 * and the bulk GeoJSON import), so all of them treat a missing configuration and an
 * AgStack failure the same way: the plot is saved without a geo id and the user can
 * regenerate it later from the map.
 *
 * This makes an HTTP call: callers that hold a database transaction open for many plots
 * should call it after the commit instead (see {@code PlotGeoJsonImportService}).
 */
@Component
public class PlotGeoIdGenerator {

	private static final Logger logger = LoggerFactory.getLogger(PlotGeoIdGenerator.class);

	private final AgStackClientService agStackClientService;

	public PlotGeoIdGenerator(AgStackClientService agStackClientService) {
		this.agStackClientService = agStackClientService;
	}

	public boolean isEnabled() {
		return agStackClientService.isEnabled();
	}

	/**
	 * @return the geo id, or {@code null} when the coordinates are not a polygon, AgStack
	 *         is not configured, or the registration failed (the failure is logged)
	 */
	public String generate(List<PlotCoordinate> coordinatesSet) {

		List<PlotCoordinate> coordinates = new ArrayList<>(coordinatesSet);

		if (coordinates.size() < 3) {
			return null;
		}

		// Deployments without AgStack credentials simply have no geo id; attempting the
		// call would log one login failure per saved plot.
		if (!agStackClientService.isEnabled()) {
			return null;
		}

		try {
			ApiRegisterFieldBoundaryResponse response = agStackClientService.registerFieldBoundaryResponse(coordinates);
			if (!CollectionUtils.isEmpty(response.getMatchedGeoIDs())) {
				// On errors API returns additional message
				if (response.getMessage() != null) {
					logger.error(response.getMessage());
				}
				return response.getMatchedGeoIDs().stream().findFirst().orElse(null);
			} else {
				// On errors API returns additional message
				if (response.getGeoID() == null && response.getMessage() != null) {
					logger.error(response.getMessage());
				}
				return response.getGeoID();
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.error("Error while generating plot geoid");
		}

		return null;
	}

}
