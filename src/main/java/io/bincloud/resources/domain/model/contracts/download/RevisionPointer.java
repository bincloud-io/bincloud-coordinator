package io.bincloud.resources.domain.model.contracts.download;

import java.util.Optional;

public interface RevisionPointer {
	public Optional<Long> getResourceId();
	public Optional<String> getFileId();
}
