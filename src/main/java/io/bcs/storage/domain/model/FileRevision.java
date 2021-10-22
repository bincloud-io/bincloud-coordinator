package io.bcs.storage.domain.model;

import java.time.Instant;

import io.bcs.common.domain.model.generator.SequentialGenerator;
import io.bcs.storage.domain.model.FileRevision.FileRevisionState.RootContext;
import io.bcs.storage.domain.model.contexts.FileDownloadingContext;
import io.bcs.storage.domain.model.contexts.FileUploadingContext;
import io.bcs.storage.domain.model.contracts.FileDescriptor;
import io.bcs.storage.domain.model.contracts.upload.FileAttributes;
import io.bcs.storage.domain.model.states.DisposedFileRevisionState;
import io.bcs.storage.domain.model.states.DraftFileRevisionState;
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
public class FileRevision implements FileDescriptor {
	@EqualsAndHashCode.Include
	private String filesystemName;

	private String fileName;
	private String mediaType;
	private String contentDisposition;
	private Instant creationMoment;
	private Instant lastModification;
	@Getter(value = AccessLevel.NONE)
	private FileRevisionState state;
	private Long fileSize;

	public FileRevision(SequentialGenerator<String> filesystemNameGenerator, FileAttributes fileAttributes) {
		super();
		this.filesystemName = filesystemNameGenerator.nextValue();
		this.fileName = fileAttributes.getFileName();
		this.mediaType = fileAttributes.getMediaType();
		this.contentDisposition = fileAttributes.getContentDisposition();
		this.state = new DraftFileRevisionState();
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
		this.state = new DisposedFileRevisionState();
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
				return FileRevision.this.filesystemName;
			}

			@Override
			public void setSize(Long size) {
				FileRevision.this.fileSize = size;
			}

			@Override
			public void setState(FileRevisionState fileState) {
				FileRevision.this.state = fileState;
			}
		};
	}

	public interface FileRevisionState {
		public String getStatus();

		public void createFile(RootContext context, FilesystemAccessor fileSystem);

		public void uploadFile(RootContext context, FileUploadingContext uploadingContext);

		public void startDistribution(RootContext context);

		public void downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset,
				Long size);

		public interface RootContext {
			public String getFileName();

			public void setSize(Long size);

			public void setState(FileRevisionState fileState);
		}
	}
}
