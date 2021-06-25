package io.bincloud.resources.domain.model.contracts;

import java.util.Optional;

public interface Range {
	public Optional<Long> getStart();

	public Optional<Long> getEnd();
}
