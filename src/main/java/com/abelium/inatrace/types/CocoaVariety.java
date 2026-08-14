package com.abelium.inatrace.types;

/**
 * Cocoa variety grown on a farmer plot.
 *
 * The specification described this catalogue as "the values 1 and 2, representing
 * Organico and CCN51". It is modelled as an enum rather than a raw 1/2 integer so the
 * stored value says what it means; the numbering stays a presentation concern, handled
 * by the client and by the export translation keys
 * (export.plots.column.cocoaVariety.value.*).
 */
public enum CocoaVariety {

	ORGANICO,

	CCN51
}
