package io.bincloud.resources.domain.model.file;

public interface FileUploadsHistory {
	public void registerFileUpload(Long resourceId, String fileId);

	public void clearUploadsHistory(Long resourceId);
	
	public void truncateUploadsHistory(Long resourceId, Long length);

}
