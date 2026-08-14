package com.abelium.inatrace.types;

/**
 * Cocoa variety grown on a farmer plot.
 *
 * The specification described this catalogue as "the values 1 and 2, representing
 * Organico and CCN51". It is modelled as an enum rather than a raw integer so the
 * stored value says what it means; getCode() keeps the agreed numeric identity
 * available for exports and integrations that expect 1 / 2.
 */
public enum CocoaVariety {

	ORGANICO(1),

	CCN51(2);

	private final int code;

	CocoaVariety(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
