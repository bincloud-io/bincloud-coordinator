package io.bcs.storage.domain.model;

import io.bcs.common.domain.model.io.transfer.DestinationPoint;
import io.bcs.common.domain.model.io.transfer.SourcePoint;

public interface FilesystemAccessor {
	public void createFile(String fileName);
	
	public SourcePoint getAccessOnRead(String fileName, Long offset, Long size);
	
	public DestinationPoint getAccessOnWrite(String fileName, Long contentSize);
	
	public void removeFile(String fileName);
}
