package io.bincloud.resources.application.download;

import java.util.Optional;

import io.bincloud.resources.domain.model.contracts.Range;

class NullRange implements Range {
	@Override
	public Optional<Long> getStart() {
		return Optional.empty();
	}

	@Override
	public Optional<Long> getEnd() {
		return Optional.empty();
	}
}
