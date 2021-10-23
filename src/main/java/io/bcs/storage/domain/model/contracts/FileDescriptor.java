package io.bcs.storage.domain.model.contracts;

import java.time.Instant;

public interface FileDescriptor {
	public String getRevisionName();
	public String getStatus();
	public String getFileName();
	public String getMediaType();
	public String getContentDisposition();
	public Instant getCreationMoment();
	public Instant getLastModification();
	public Long getFileSize();
}
