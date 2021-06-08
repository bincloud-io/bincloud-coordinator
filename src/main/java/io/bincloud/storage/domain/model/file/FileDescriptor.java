package io.bincloud.storage.domain.model.file;

import io.bincloud.common.domain.model.time.DateTime;

public interface FileDescriptor {
	public String getStatus();
	public DateTime getCreationMoment();
	public DateTime getLastModification();
	public Long getSize();
}
