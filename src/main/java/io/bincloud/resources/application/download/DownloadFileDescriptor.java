package io.bincloud.resources.application.download;

import java.util.function.Supplier;

import io.bincloud.files.domain.model.FileDescriptor;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.application.providers.ExistingFileDescriptorProvider;
import io.bincloud.resources.application.providers.ExistingResourceProvider;
import io.bincloud.resources.application.providers.SpecifiedFileIdentifierProvider;
import io.bincloud.resources.application.providers.UnspecifiedFileIdentifierProvider;
import io.bincloud.resources.domain.model.Resource;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.FileDownloader;
import io.bincloud.resources.domain.model.contracts.FileDownloader.FileDownloadContext;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DownloadFileDescriptor implements FileDownloader.DownloadedFile {
	@Getter
	@EqualsAndHashCode.Include
	private final String fileId;
	private final FileDescriptor fileDescriptor;
	private final Resource resource;

	public DownloadFileDescriptor(FileDownloadContext fileDownloadContext, FileStorage fileStorage,
			FileUploadsHistory fileUploadHistory, ResourceRepository resourceRepository) {
		super();
		this.resource = createResourceProvider(fileDownloadContext, resourceRepository).get();
		this.fileId = createFileIdentifierProvider(fileDownloadContext, () -> resource.getId(), fileUploadHistory).get();
		Supplier<FileDescriptor> fileDescriptorProvider = new ExistingFileDescriptorProvider(fileId, fileStorage);
		this.fileDescriptor = fileDescriptorProvider.get();
	}

	@Override
	public Long getTotalLength() {
		return fileDescriptor.getSize();
	}

	@Override
	public String getFileName() {
		return resource.getFileName();
	}

	private static Supplier<Resource> createResourceProvider(FileDownloadContext fileDownloadContext, ResourceRepository resourceRepository) {
		return new ExistingResourceProvider(fileDownloadContext.getResourceId(), resourceRepository);
	}

	private static Supplier<String> createFileIdentifierProvider(FileDownloadContext fileDownloadContext, Supplier<Long> resourceIdProvider, FileUploadsHistory fileUploadsHistory) {
		return fileDownloadContext.getFileId().<Supplier<String>>map(
				fileId -> new SpecifiedFileIdentifierProvider(resourceIdProvider, fileId, fileUploadsHistory))
				.orElse(new UnspecifiedFileIdentifierProvider(resourceIdProvider, fileUploadsHistory));
	}
}
