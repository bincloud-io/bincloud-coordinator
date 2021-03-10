package io.bincloud.storage.domain.model.file;

import java.time.Instant;

public interface FileDescriptor {
	public String getStatus();
	public Instant getCreationMoment();
	public Instant getLastModification();
	public Long getSize();
}
