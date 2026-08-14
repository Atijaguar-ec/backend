package com.abelium.inatrace.types;

/**
 * Certification scheme held by a farmer plot.
 *
 * The four values are a closed catalogue defined by the organization; they are stored
 * as strings so the DB stays readable, and rendered with their commercial label
 * (slashes included) on the client.
 *
 * Note for the persistence mapping: the longest constant is 49 characters, which does
 * NOT fit in Lengths.ENUM (40). The column is declared with Lengths.DEFAULT instead.
 */
public enum PlotCertificationType {

	ORGANICO_UE_NOP_BIOSUISSE_NATURLAND_FAIRTRADE_SPP,

	ORGANICO_UE_NOP_BIOSUISSE_FAIRTRADE_SPP,

	ORGANICO_UE_NOP_FAIRTRADE_SPP,

	CONVENCIONAL_FAIRTRADE
}
