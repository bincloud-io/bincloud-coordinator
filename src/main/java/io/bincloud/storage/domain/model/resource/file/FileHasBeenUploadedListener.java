package io.bincloud.storage.domain.model.resource.file;

import io.bincloud.common.domain.model.event.EventListener;
import io.bincloud.common.domain.model.time.DateTime;
import io.bincloud.storage.domain.model.resource.FileHasBeenUploaded;
import io.bincloud.storage.domain.model.resource.file.FileUploading.InitialState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileHasBeenUploadedListener implements EventListener<FileHasBeenUploaded> {
	private final FileUploadingRepository fileUploadingRepository;
	
	@Override
	public void onEvent(FileHasBeenUploaded event) {
		FileUploading fileUploading = new FileUploading(new FileUploadingInitialState(event));
		fileUploadingRepository.save(fileUploading);
	}
	
	@RequiredArgsConstructor
	private class FileUploadingInitialState implements InitialState {
		private final FileHasBeenUploaded event;
		
		@Override
		public Long getResourceId() {
			return event.getResourceId();
		}

		@Override
		public String getFileId() {
			return event.getFileId();
		}

		@Override
		public DateTime getUploadingMoment() {
			return event.getUploadingMoment();
		}
	}
}
