package io.bincloud.resources.application.download;

import java.time.Instant;
import java.util.function.Supplier;

import io.bincloud.files.domain.model.FileDescriptor;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.application.ExistingFileDescriptorProvider;
import io.bincloud.resources.application.ExistingResourceProvider;
import io.bincloud.resources.application.SpecifiedFileIdentifierProvider;
import io.bincloud.resources.application.UnspecifiedFileIdentifierProvider;
import io.bincloud.resources.domain.model.Resource;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.download.FileRevisionDescriptor;
import io.bincloud.resources.domain.model.contracts.download.RevisionPointer;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.Getter;

public class StoredFileRevisionDescriptor implements FileRevisionDescriptor {
	@Getter
	private String fileId;
	private Resource resource;
	private FileDescriptor fileDescriptor;

	@Override
	public Long getResourceId() {
		return resource.getId();
	}

	@Override
	public String getFileName() {
		return resource.getFileName();
	}

	@Override
	public String getStatus() {
		return fileDescriptor.getStatus();
	}

	@Override
	public Instant getCreationMoment() {
		return fileDescriptor.getCreationMoment();
	}

	@Override
	public Instant getLastModification() {
		return fileDescriptor.getLastModification();
	}

	@Override
	public Long getFileSize() {
		return fileDescriptor.getSize();
	}

	public StoredFileRevisionDescriptor(RevisionPointer fileDownloadContext, FileStorage fileStorage,
			FileUploadsHistory fileUploadHistory, ResourceRepository resourceRepository) {
		super();
		this.resource = createResourceProvider(fileDownloadContext, resourceRepository).get();
		this.fileId = createFileIdentifierProvider(fileDownloadContext, () -> resource.getId(), fileUploadHistory).get();
		Supplier<FileDescriptor> fileDescriptorProvider = new ExistingFileDescriptorProvider(fileId, fileStorage);
		this.fileDescriptor = fileDescriptorProvider.get();
	}

	private static Supplier<Resource> createResourceProvider(RevisionPointer fileDownloadContext,
			ResourceRepository resourceRepository) {
		return new ExistingResourceProvider(fileDownloadContext.getResourceId(), resourceRepository);
	}

	private static Supplier<String> createFileIdentifierProvider(RevisionPointer fileDownloadContext,
			Supplier<Long> resourceIdProvider, FileUploadsHistory fileUploadsHistory) {
		return fileDownloadContext.getFileId()
				.<Supplier<String>>map(
						fileId -> new SpecifiedFileIdentifierProvider(resourceIdProvider, fileId, fileUploadsHistory))
				.orElse(new UnspecifiedFileIdentifierProvider(resourceIdProvider, fileUploadsHistory));
	}
}
