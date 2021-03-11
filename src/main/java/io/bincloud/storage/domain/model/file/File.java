package io.bincloud.storage.domain.model.file;

import java.time.Instant;

import io.bincloud.storage.domain.model.file.FileState.RootContext;
import io.bincloud.storage.domain.model.file.states.DisposedState;
import io.bincloud.storage.domain.model.file.states.DraftState;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
public class File implements FileDescriptor {
	@EqualsAndHashCode.Include
	private final String fileId;
	private Instant creationMoment;
	private Instant lastModification;
	@Getter(value = AccessLevel.NONE)
	private FileState state;
	private Long size;

	public File(@NonNull IdGenerator idGenerator) {
		super();
		this.fileId = idGenerator.generateId();
		this.state = new DraftState();
		this.creationMoment = Instant.now();
		this.lastModification = this.creationMoment;
		this.size = 0L;
	}
	
	public String getStatus() {
		return state.getStatus();
	}

	public void createFile(FilesystemAccessor fileSystem) {
		this.state = state.createFile(createRootContext(), fileSystem);
		updateModification();
	}

	public void uploadFile(FileUploadingContext uploadingContext) {
		this.state = state.uploadFile(createRootContext(), uploadingContext);
	}
	
	public void startDistribution(FilesystemAccessor fileSystem) {
		this.state = state.startDistribution(createRootContext(), fileSystem);
		updateModification();
	}

	public void downloadFile(FileDownloadingContext downloadingContext) {
		downloadFileRange(downloadingContext, 0L, getSize());
	}
	
	public void downloadFileRange(FileDownloadingContext downloadingContext, Long offset, Long size) {
		this.state = state.downloadFile(createRootContext(), downloadingContext, offset, size);
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
		};
	}
	
	public interface IdGenerator {
		public String generateId();
	}
}
