package io.bcs.storage.domain.model;

import java.time.Instant;

import io.bce.promises.Promise;
import io.bcs.common.domain.model.generator.SequentialGenerator;
import io.bcs.storage.domain.model.FileRevision.ContentUploader.UploadedContent;
import io.bcs.storage.domain.model.FileRevision.FileRevisionState.RootContext;
import io.bcs.storage.domain.model.contexts.FileDownloadingContext;
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

@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FileRevision {
	@Getter
	@EqualsAndHashCode.Include
	private FileId revisionName;
	private String fileName;
	private String mediaType;
	private String contentDisposition;
	private Instant creationMoment;
	private Instant lastModification;
	@Getter(value = AccessLevel.NONE)
	private FileRevisionState state;
	private Long fileSize;

	public FileRevision(SequentialGenerator<FileId> revisionNameGenerator, FileAttributes fileAttributes) {
		super();
		this.revisionName = revisionNameGenerator.nextValue();
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
	
	public FileDescriptor getDescriptor() {
		return new FileDescriptor() {
			@Override
			public String getStatus() {
				return FileRevision.this.getStatus();
			}
			
			@Override
			public String getRevisionName() {
				return revisionName.getFilesystemName();
			}
			
			@Override
			public String getMediaType() {
				return mediaType;
			}
			
			@Override
			public Instant getLastModification() {
				return lastModification;
			}
			
			@Override
			public Long getFileSize() {
				return fileSize;
			}
			
			@Override
			public String getFileName() {
				return fileName;
			}
			
			@Override
			public Instant getCreationMoment() {
				return creationMoment;
			}
			
			@Override
			public String getContentDisposition() {
				return contentDisposition;
			}
		};
	}
	
	
	public void createFile(FilesystemAccessor fileSystem) {
		state.createFile(createRootContext(), fileSystem);
		updateModification();
	}
	
	public Promise<FileDescriptor> uploadContent(ContentUploader contentUploader) {
		return state.uploadContent(createRootContext(), contentUploader).chain((uploadedContent, deferred) -> {
			updateFileSize(fileSize);
			deferred.resolve(getDescriptor());
		});
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
		filesystemAccessor.removeFile(revisionName.getFilesystemName());
		updateModification();
	}

	private void updateModification() {
		this.lastModification = Instant.now();
	}
	
	private void updateFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	private RootContext createRootContext() {
		return new RootContext() {
			@Override
			public String getRevisionName() {
				return FileRevision.this.revisionName.getFilesystemName();
			}

			@Override
			public void setSize(Long size) {
				updateFileSize(size);
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
		
		public Promise<UploadedContent> uploadContent(RootContext context, ContentUploader contentUploader);
		
		public void startDistribution(RootContext context);

		public void downloadFile(RootContext context, FileDownloadingContext downloadingContext, Long offset,
				Long size);

		public interface RootContext {
			public String getRevisionName();

			public void setSize(Long size);

			public void setState(FileRevisionState fileState);
		}
	}
	
	
	public interface ContentUploader {
		public Promise<UploadedContent> upload(String revisionName);
		
		public interface UploadedContent {
			public Long getSize();
		}
	}
	
}
