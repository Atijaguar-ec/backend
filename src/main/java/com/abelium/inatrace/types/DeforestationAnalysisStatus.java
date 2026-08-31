package com.abelium.inatrace.types;

/**
 * Lifecycle of one Whisp deforestation analysis job.
 *
 * Whisp runs the analysis asynchronously on Google Earth Engine: submitting returns a
 * token straight away and the result arrives later, so the job state has to be stored
 * on our side. Whisp itself keeps results only in temporary storage (its progress
 * snapshots expire after ten minutes), which is why the outcome is persisted here.
 */
public enum DeforestationAnalysisStatus {

	/**
	 * Submitted to Whisp, no progress reported yet.
	 */
	PENDING,

	/**
	 * Whisp is processing the geometry (it reports a completion percentage).
	 */
	IN_PROGRESS,

	/**
	 * Finished; the indicators and the raw result are stored.
	 */
	COMPLETED,

	/**
	 * The submission or the analysis failed; the reason is in {@code errorMessage}.
	 */
	FAILED
}
