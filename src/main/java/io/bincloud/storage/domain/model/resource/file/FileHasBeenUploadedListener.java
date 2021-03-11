package io.bincloud.storage.domain.model.resource.file;

import io.bincloud.common.event.EventListener;
import io.bincloud.storage.domain.model.resource.FileHasBeenUploaded;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileHasBeenUploadedListener implements EventListener<FileHasBeenUploaded> {
	private final FileUploadingRepository fileUploadingRepository;
	
	@Override
	public void onEvent(FileHasBeenUploaded event) {
		throw new UnsupportedOperationException();
	}
}
