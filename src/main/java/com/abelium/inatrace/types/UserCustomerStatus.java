package com.abelium.inatrace.types;

/**
 * Status of a user customer (farmer / collector) within the organization.
 *
 * Transitions:
 *   ACTIVE    -> SUSPENDED, RETIRED
 *   SUSPENDED -> ACTIVE, RETIRED
 *   RETIRED   -> ACTIVE (the farmer may re-join the organization)
 */
public enum UserCustomerStatus {

	/**
	 * The farmer is eligible for any transaction within the organization.
	 */
	ACTIVE,

	/**
	 * Temporary suspension. No transaction can be made with the farmer, so the farmer
	 * is not offered when selecting a producer. Can be set back to ACTIVE or moved to RETIRED.
	 */
	SUSPENDED,

	/**
	 * The farmer left the organization and is not eligible for any transaction.
	 * Can be set back to ACTIVE if the farmer re-joins.
	 */
	RETIRED;

	/**
	 * Whether transactions can be made with a user customer in this status.
	 */
	public boolean allowsTransactions() {
		return this == ACTIVE;
	}

	/**
	 * Whether a change from this status to the given one is allowed.
	 * Keeping the same status is always allowed (updates that don't touch the status).
	 */
	public boolean canTransitionTo(UserCustomerStatus target) {

		if (target == null) {
			return false;
		}

		if (this == target) {
			return true;
		}

		return switch (this) {
			case ACTIVE, SUSPENDED -> true;
			// A retired farmer re-joins as ACTIVE; suspension only applies to an ongoing relationship.
			case RETIRED -> target == ACTIVE;
		};
	}
}
