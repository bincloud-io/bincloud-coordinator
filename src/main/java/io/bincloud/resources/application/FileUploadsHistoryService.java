package io.bincloud.resources.application;

import java.time.Instant;

import io.bincloud.files.domain.model.FileDescriptor;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.domain.model.errors.UploadedFileDescriptorHasNotBeenFoundException;
import io.bincloud.resources.domain.model.file.FileUpload;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import io.bincloud.resources.domain.model.file.FileUploadsRepository;
import io.bincloud.resources.domain.model.file.FileUpload.InitialState;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileUploadsHistoryService implements FileUploadsHistory {
	private final FileStorage fileStorage;
	private final FileUploadsRepository fileUploadsRepository;

	@Override
	public void registerFileUpload(Long resourceId, String fileId) {
		FileUpload fileUpload = fileStorage.getFileDescriptor(fileId)
				.map(descriptor -> new DescriptorBasedFileUploadInitialState(resourceId, fileId, descriptor))
				.map(initialState -> new FileUpload(initialState))
				.orElseThrow(() -> new UploadedFileDescriptorHasNotBeenFoundException());
		fileUploadsRepository.save(fileUpload);
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
	@RequiredArgsConstructor
	private class DescriptorBasedFileUploadInitialState implements InitialState {
		@Getter
		private final Long resourceId;
		@Getter
		private final String fileId;
		private final FileDescriptor fileDescriptor;

		@Override
		public Instant getUploadMoment() {
			return fileDescriptor.getLastModification();
		}
	}
}
