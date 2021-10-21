package io.bce.interaction.polling;

import java.util.Collection;

@FunctionalInterface
public interface BatchPoller<D> {
	public Collection<D> poll();
}
