package io.bincloud.storage.domain.model.file;

import java.util.Optional;

import io.bincloud.common.io.transfer.DestinationPoint;
import io.bincloud.common.io.transfer.SourcePoint;

public interface FileStorage {
	public String createNewFile();
	
	public Optional<FileDescriptor> getFileDescriptor(String fileId);
	
	public void uploadFile(String fileId, SourcePoint source);
	
	public void downloadFile(String fileId, DestinationPoint destination);
}
