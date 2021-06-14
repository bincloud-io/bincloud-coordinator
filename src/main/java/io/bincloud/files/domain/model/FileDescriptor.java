package io.bincloud.files.domain.model;

import java.time.Instant;

public interface FileDescriptor {
	public String getStatus();
	public Instant getCreationMoment();
	public Instant getLastModification();
	public Long getSize();
}
