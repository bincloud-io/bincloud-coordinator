package io.bincloud.resources.domain.model.resource.history;

import io.bincloud.resources.domain.model.FileReference;

public interface FileHistory {
	
	/**
	 * Register uploaded file
	 * 
	 * @param command The uploaded file registration command
	 * @return The file reference
	 */
	public FileReference registerUploadedFile(RegisterFileUpload command);

	public void makeUploadedFileAvailable(MakeUploadedFileAvailable command);

	public void truncateUploadHistory(TruncateUploadHistory command);
}
