package io.bce.interaction.interactor;

import io.bce.URN;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This class represents the target address value that identifies the component
 * in the system that provides an API to communicate with.
 * 
 * @author Dmitry Mikhaylenko
 *
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class TargetAddress {
	private final URN urn;
	
	@Override
	public String toString() {
		return urn.toString();
	}

	/**
	 * Create the target address of an URN-address string value
	 * 
	 * @param urnAddress The URN-address string value
	 * @return The target address
	 */
	public static final TargetAddress ofURN(@NonNull String urnAddress) {
		return ofURN(URN.ofURN(urnAddress));
	}
	
	private static final TargetAddress ofURN(@NonNull URN urn) {
		return new TargetAddress(urn);
	}
}
