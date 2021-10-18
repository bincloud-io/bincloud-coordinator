package io.bincloud.resources.domain.model.resource.history;

import io.bincloud.resources.domain.model.FileReference;
import io.bincloud.resources.domain.model.resource.history.FileStorage.CreateFileInStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileHistoryService implements FileHistory {
	private final UploadedFileRepository fileUploadsRepository;
	private final FileStorage fileStorage;
		
	@Override
	public FileReference registerUploadedFile(RegisterFileUpload command) {
		FileReference reference = createEmptyFileInTheFileStorage(command);
		fileUploadsRepository.save(new UploadedFile(reference));
		return reference;
	}

	@Override
	public void makeUploadedFileAvailable(MakeUploadedFileAvailable command) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void truncateUploadHistory(TruncateUploadHistory command) {
		throw new UnsupportedOperationException();
	}
	
	private FileReference createEmptyFileInTheFileStorage(RegisterFileUpload command) {	
		return fileStorage.createFile(new CreateFileInStorage() {			
			@Override
			public Long getResourceId() {
				return command.getResourceId();
			}
			
			@Override
			public String getMediaType() {
				return command.getMediaType();
			}
			
			@Override
			public String getFileName() {
				return command.getFileName();
			}
			
			@Override
			public String getContentDisposition() {
				return command.getContentDisposition();
			}
		});
	}
}
