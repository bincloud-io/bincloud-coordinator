package io.bce.actor;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "wrap")
public class ActorName {
	@NonNull
	private final String name;

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Derive the actor name from the current actor name with another actor name.
	 * The result actor name will include as the current name as the another actor
	 * name.
	 * 
	 * @param actorName The actor name
	 * @return The derived actor name
	 */
	public ActorName deriveWith(@NonNull ActorName actorName) {
		return new ActorName(String.format("%s.%s", this, actorName));
	}
}
