package io.bcs.files.domain.model;

public interface FileState {
	public String getStatus();
	public void createFile(RootContext context, FilesystemAccessor fileSystem);
	public void uploadFile(RootContext context, FileUploadingContext uploadingContext);
	public void startDistribution(RootContext context);
	public void downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset, Long size);
	
	public interface RootContext {
		public String getFileName();
		public void setSize(Long size);
		public void setState(FileState fileState);
	}
}