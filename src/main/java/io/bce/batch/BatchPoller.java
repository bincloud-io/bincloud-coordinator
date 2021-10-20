package io.bce.batch;

import java.util.Collection;

@FunctionalInterface
public interface BatchPoller<D> {
	public Collection<D> poll();
}
