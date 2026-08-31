package com.abelium.inatrace.components.agstack;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.agstack.api.ApiRegisterFieldBoundaryErrorResponse;
import com.abelium.inatrace.components.agstack.api.ApiRegisterFieldBoundaryRequest;
import com.abelium.inatrace.components.agstack.api.ApiRegisterFieldBoundaryResponse;
import com.abelium.inatrace.db.entities.common.PlotCoordinate;
import com.abelium.inatrace.tools.PlotGeometryTools;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class AgStackClientService {

	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

	@Value("${INATrace.agstack.baseURL:}")
	private String baseURL;

    private final AgStackClientTokenManager tokenService;

    public AgStackClientService(AgStackClientTokenManager tokenService) {
        this.tokenService = tokenService;
    }

	/**
	 * Whether a geo id can be requested at all: both the boundary endpoint and the login
	 * credentials have to be configured.
	 */
	public boolean isEnabled() {
		return StringUtils.isNotBlank(baseURL) && tokenService.isEnabled();
	}

	public ApiRegisterFieldBoundaryResponse registerFieldBoundaryResponse(List<PlotCoordinate> plotCoordinates) throws ApiException {

		if (!isEnabled()) {
			throw new ApiException(ApiStatus.INVALID_REQUEST, "AgStack integration is not configured");
		}

		String wkt = PlotGeometryTools.toPolygonWkt(plotCoordinates);
		if (wkt == null) {
			throw new ApiException(ApiStatus.INVALID_REQUEST,
					"The plot does not have enough valid coordinates to form a polygon");
		}

		ApiRegisterFieldBoundaryRequest request = new ApiRegisterFieldBoundaryRequest();
		request.setS2Index("8, 13");
		request.setWkt(wkt);

        String token = this.tokenService.retrieveToken();
        if (token == null) {
            throw new ApiException(ApiStatus.ERROR, "Error while retrieving api token");
        }

		WebClient webClient = WebClient.create(baseURL);

		return webClient
				.post()
				.uri(uriBuilder -> uriBuilder.path("/register-field-boundary").build())
				.body(Mono.just(request), ApiRegisterFieldBoundaryRequest.class)
				.header("Authorization", "Bearer " + token)
				.header("X-FROM-ASSET-REGISTRY", "True")
				.accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    // 1. Handle 400 BAD REQUEST, because of api implementation,
                    // In this request "matched geo ids" can be returned, with error message
                    // so it is mapped in response class
                    if (response.statusCode().equals(HttpStatus.BAD_REQUEST)) {
                        return response.bodyToMono(ApiRegisterFieldBoundaryResponse.class);
                    }
                    // 2. Handle 200 OK (and other success codes)
                    else if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(ApiRegisterFieldBoundaryResponse.class);
                    }
                    // 3. Handle errors, throw exception
                    else {
                        return response.bodyToMono(ApiRegisterFieldBoundaryErrorResponse.class)
                                .flatMap(error -> Mono.error(new ApiException(ApiStatus.ERROR, error.getError())));
                    }
                })
				// Bounded: this call runs inside the transaction that saves the plot.
				.block(REQUEST_TIMEOUT);
	}

}
