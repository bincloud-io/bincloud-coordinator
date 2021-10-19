package io.bcs.storage.domain.model.contracts.download;

import java.util.Optional;

public interface Range {
	public Optional<Long> getStart();

	public Optional<Long> getEnd();
}
