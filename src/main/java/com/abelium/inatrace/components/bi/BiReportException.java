package com.abelium.inatrace.components.bi;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Domain exception with explicit HTTP status for Superset BI gateway responses.
 */
public class BiReportException extends ApiException {

	private static final long serialVersionUID = 1L;

	public BiReportException(HttpStatus httpStatus, ApiStatus apiStatus, String errorMessage) {
		super(httpStatus, apiStatus, errorMessage);
	}
}
