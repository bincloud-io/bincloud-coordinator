package io.bincloud.files.domain.model;

import java.time.Instant;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.files.domain.model.FileState.RootContext;
import io.bincloud.files.domain.model.contracts.FileDescriptor;
import io.bincloud.files.domain.model.contracts.upload.FileAttributes;
import io.bincloud.files.domain.model.states.DisposedState;
import io.bincloud.files.domain.model.states.DraftState;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class File implements FileDescriptor {
	@EqualsAndHashCode.Include
	private String filesystemName;
	
	private String fileName;
	private String mediaType;
	private String contentDisposition;
	private Instant creationMoment;
	private Instant lastModification;
	@Getter(value = AccessLevel.NONE)
	private FileState state;
	private Long fileSize;

	public File(SequentialGenerator<String> filesystemNameGenerator, FileAttributes fileAttributes) {
		super();
		this.filesystemName = filesystemNameGenerator.nextValue();
		this.fileName = fileAttributes.getFileName();
		this.mediaType = fileAttributes.getMediaType();
		this.contentDisposition = fileAttributes.getContentDisposition();
		this.state = new DraftState();
		this.creationMoment = Instant.now();
		this.lastModification = this.creationMoment;
		this.fileSize = 0L;
	}
	
	public String getStatus() {
		return state.getStatus();
	}
	
	public void createFile(FilesystemAccessor fileSystem) {
		state.createFile(createRootContext(), fileSystem);
		updateModification();
	}

	public void uploadFileContent(FileUploadingContext uploadingContext) {
		state.uploadFile(createRootContext(), uploadingContext);
	}
	
	public void startDistribution() {
		state.startDistribution(createRootContext());
		updateModification();
	}
	
	public void downloadFileContent(FileDownloadingContext downloadingContext, Long offset, Long size) {
		state.downloadFile(createRootContext(), downloadingContext, offset, size);
	}

	public void dispose(FilesystemAccessor filesystemAccessor) {
		this.state = new DisposedState();
		filesystemAccessor.removeFile(filesystemName);
		updateModification();
	}
	
	private void updateModification() {
		this.lastModification = Instant.now();
	}
	
	private RootContext createRootContext() {
		return new RootContext() {
			@Override
			public String getFileName() {
				return File.this.filesystemName;
			}
			
			@Override
			public void setSize(Long size) {
				File.this.fileSize = size;
			}

			@Override
			public void setState(FileState fileState) {
				File.this.state = fileState;
			}
		};
	}
}
