package io.bincloud.storage.domain.model.file;

public interface FileState {
	public String getStatus();
	public FileState createFile(RootContext context, FilesystemAccessor fileSystem);
	public FileState uploadFile(RootContext context, FileUploadingContext uploadingContext);
	public FileState startDistribution(RootContext context, FilesystemAccessor fileSystem);
	public FileState downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset, Long size);
	
	public interface RootContext {
		public String getFileName();
		public void setSize(Long size);
	}
} 

