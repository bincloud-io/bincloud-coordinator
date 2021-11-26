package io.bcs.domain.model.file.fragments;

import java.util.Optional;

public interface Range {
	public Optional<Long> getStart();

	public Optional<Long> getEnd();
}
