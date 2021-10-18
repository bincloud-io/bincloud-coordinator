package io.bincloud.resources.domain.model.resource.history;

import java.time.Instant;

public interface MakeUploadedFileAvailable {
	public Long getResourceId();

	public Instant getDistributionStartingDate();
	
	public Long getFileSize();
}
