package io.bincloud.storage.domain.model.file;

import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;

public interface FilesystemAccessor {
	public void createFile(String fileName);
	
	public Long getFileSize(String fileName);
	
	public SourcePoint getAccessOnRead(String fileName, Long offset, Long size);
	
	public DestinationPoint getAccessOnWrite(String fileName);
}
