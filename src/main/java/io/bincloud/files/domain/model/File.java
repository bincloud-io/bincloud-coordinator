package io.bincloud.files.domain.model;

import java.time.Instant;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.files.domain.model.FileState.RootContext;
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
	private String fileId;
	private Instant creationMoment;
	private Instant lastModification;
	@Getter(value = AccessLevel.NONE)
	private FileState state;
	private Long size;

	public File(SequentialGenerator<String> idGenerator) {
		super();
		this.fileId = idGenerator.nextValue();
		this.state = new DraftState();
		this.creationMoment = Instant.now();
		this.lastModification = this.creationMoment;
		this.size = 0L;
	}
	
	public String getStatus() {
		return state.getStatus();
	}
	
	public void createFile(FilesystemAccessor fileSystem) {
		state.createFile(createRootContext(), fileSystem);
		updateModification();
	}

	public void uploadFile(FileUploadingContext uploadingContext) {
		state.uploadFile(createRootContext(), uploadingContext);
	}
	
	public void startDistribution(FilesystemAccessor fileSystem) {
		state.startDistribution(createRootContext(), fileSystem);
		updateModification();
	}
	
	public void downloadFileContent(FileDownloadingContext downloadingContext, Long offset, Long size) {
		state.downloadFile(createRootContext(), downloadingContext, offset, size);
	}

	public void dispose() {
		this.state = new DisposedState();
		updateModification();
	}
	
	private void updateModification() {
		this.lastModification = Instant.now();
	}
	
	private RootContext createRootContext() {
		return new RootContext() {
			@Override
			public String getFileName() {
				return File.this.fileId;
			}
			
			@Override
			public void setSize(Long size) {
				File.this.size = size;
			}

			@Override
			public void setState(FileState fileState) {
				File.this.state = fileState;
			}
		};
	}
}
