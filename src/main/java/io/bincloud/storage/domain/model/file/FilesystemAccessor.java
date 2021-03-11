package io.bincloud.storage.domain.model.file;

import io.bincloud.common.io.transfer.DestinationPoint;
import io.bincloud.common.io.transfer.SourcePoint;

public interface FilesystemAccessor {
	public void createFile(String fileName);
	
	public Long getFileSize(String fileName);
	
	public SourcePoint getAccessOnRead(String fileName, Long offset, Long size);
	
	public DestinationPoint getAccessOnWrite(String fileName);
}
