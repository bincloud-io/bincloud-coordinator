package io.bincloud.resources.application;

import java.time.Instant;
import java.util.Optional;

import io.bincloud.files.domain.model.FileDescriptor;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.domain.model.file.FileUpload;
import io.bincloud.resources.domain.model.file.FileUpload.InitialState;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import io.bincloud.resources.domain.model.file.FileUploadsRepository;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileUploadsHistoryService implements FileUploadsHistory {
	private final FileStorage fileStorage;
	private final FileUploadsRepository fileUploadsRepository;

	@Override
	public void registerFileUpload(Long resourceId, String fileId) {
		InitialState fileUploadInitialState = new DescriptorBasedFileUploadInitialState(resourceId, fileId);
		fileUploadsRepository.save(new FileUpload(fileUploadInitialState));
	}
	
	@Override
	public boolean checkFileUploadExistence(Long resourceId, String fileId) {
		return fileUploadsRepository.findById(resourceId, fileId).isPresent();
	}

	@Override
	public Optional<FileUpload> findFileUploadForResource(Long resourceId) {
		return fileUploadsRepository.findLatestResourceUpload(resourceId);
	}

	@Override
	public void truncateUploadsHistory(Long resourceId, Long length) {
		fileUploadsRepository.findAllResourceUploads(resourceId).skip(length).forEach(this::removeFileUpload);
	}

	@Override
	public void clearUploadsHistory(Long resourceId) {
		truncateUploadsHistory(resourceId, 0L);
	}

	private void removeFileUpload(FileUpload fileUpload) {
		fileUploadsRepository.remove(fileUpload.getResourceId(), fileUpload.getFileId());
		fileStorage.disposeFile(fileUpload.getFileId());
	}

	@EqualsAndHashCode
	private class DescriptorBasedFileUploadInitialState implements InitialState {
		@Getter
		private final Long resourceId;
		@Getter
		private final String fileId;
		private final FileDescriptor fileDescriptor;

		public DescriptorBasedFileUploadInitialState(Long resourceId, String fileId) {
			super();
			this.resourceId = resourceId;
			this.fileId = fileId;
			this.fileDescriptor = new ExistingFileDescriptorProvider(fileId, fileStorage).get();
		}

		@Override
		public Instant getUploadMoment() {
			return fileDescriptor.getLastModification();
		}
	}
}
