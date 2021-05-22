package io.bincloud.storage.domain.model.file;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.common.domain.model.time.DateTime;
import io.bincloud.storage.domain.model.file.FileState.RootContext;
import io.bincloud.storage.domain.model.file.states.DisposedState;
import io.bincloud.storage.domain.model.file.states.DraftState;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class File implements FileDescriptor {
	@NonNull
	@EqualsAndHashCode.Include
	private String fileId;
	private DateTime creationMoment;
	private DateTime lastModification;
	@Getter(value = AccessLevel.NONE)
	private FileState state;
	private Long size;

	public File(@NonNull SequentialGenerator<String> idGenerator) {
		super();
		this.fileId = idGenerator.nextValue();
		this.state = new DraftState();
		this.creationMoment = DateTime.now();
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

	public void downloadFile(FileDownloadingContext downloadingContext) {
		downloadFileRange(downloadingContext, 0L, getSize());
	}
	
	public void downloadFileRange(FileDownloadingContext downloadingContext, Long offset, Long size) {
		state.downloadFile(createRootContext(), downloadingContext, offset, size);
	}

	public void dispose() {
		this.state = new DisposedState();
		updateModification();
	}
	
	private void updateModification() {
		this.lastModification = new DateTime();
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
