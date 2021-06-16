package io.bincloud.resources.domain.model.file;

import java.util.Optional;

public interface FileUploadsHistory {
	public Optional<FileUpload> findFileUploadForResource(Long resourceId);
	
	public boolean checkFileUploadExistence(Long resourceId, String fileId);
	
	public void registerFileUpload(Long resourceId, String fileId);

	public void clearUploadsHistory(Long resourceId);
	
	public void truncateUploadsHistory(Long resourceId, Long length);

}
