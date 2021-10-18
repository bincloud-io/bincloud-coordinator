package io.bincloud.resources.port.adapter.integrations;

import io.bincloud.files.domain.model.FileId;
import io.bincloud.files.domain.model.contracts.FileManager;
import io.bincloud.files.domain.model.contracts.upload.FileAttributes;
import io.bincloud.resources.domain.model.FileReference;
import io.bincloud.resources.domain.model.resource.history.FileStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileStorageLocalAdapter implements FileStorage {
	private final FileManager fileManager;

	@Override
	public FileReference createFile(CreateFileInStorage command) {
		FileId fileId = fileManager.createFileRevision(new FileAttributes() {			
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
		
		return new FileReference() {
			@Override
			public Long getResourceId() {
				return command.getResourceId();
			}
			
			@Override
			public String getFilesystemName() {
				return fileId.getFilesystemName();
			}
		};
	}	
}
