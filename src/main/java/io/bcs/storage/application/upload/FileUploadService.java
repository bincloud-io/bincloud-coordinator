package io.bcs.storage.application.upload;

import java.util.Optional;

import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.binary.BinaryDestination;
import io.bce.interaction.streaming.binary.BinarySource;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.storage.domain.model.FileId;
import io.bcs.storage.domain.model.FileRevision;
import io.bcs.storage.domain.model.FileRevision.ContentUploader;
import io.bcs.storage.domain.model.FileRevisionRepository;
import io.bcs.storage.domain.model.FilesystemAccessor;
import io.bcs.storage.domain.model.contracts.FileDescriptor;
import io.bcs.storage.domain.model.contracts.upload.FileUploader;
import io.bcs.storage.domain.model.errors.UnspecifiedRevisionNameException;
import io.bcs.storage.domain.model.states.FileDoesNotExistException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileUploadService implements FileUploader {
	private final FileRevisionRepository fileRepository;
	private final FilesystemAccessor filesystemAccessor;
	private final Streamer dataStreamer;
	
	@Override
	public Promise<FileDescriptor> uploadFileContent(UploadFileCommand uploadFileCommand, BinarySource source) {
		return findRevision(uploadFileCommand.getRevisionName()).chain((fileRevision, deferred) -> {
			fileRevision.uploadContent(new StreamingContentUploader(dataStreamer, source))
				.then(fileDescriptor -> {
					fileRepository.save(fileRevision);
					deferred.resolve(fileDescriptor);
				}).error(deferred);
		});
	}
	
	private Promise<FileRevision> findRevision(Optional<FileId> receivedRevisionName) {
		return Promises.of(deferred -> {
			FileId fileId = receivedRevisionName.orElseThrow(UnspecifiedRevisionNameException::new);
			FileRevision fileRevision = fileRepository.findById(fileId).orElseThrow(FileDoesNotExistException::new);
			deferred.resolve(fileRevision);
		});
	}
	
	@RequiredArgsConstructor
	private class StreamingContentUploader implements ContentUploader {
		private final Streamer dataStreamer;
		private final BinarySource source;
		
		@Override
		public Promise<UploadedContent> upload(String revisionName) {
			return Promises.of(deferred -> {
				BinaryDestination destination = filesystemAccessor.getAccessOnWrite(revisionName);
				dataStreamer.createStream(source, destination).start()
					.error(error -> deferred.reject(error))
					.then(stat -> {
						deferred.resolve(new UploadedContent() {
							@Override
							public Long getSize() {
								return stat.getSize();
							}
						});
					});
			});
		}
	}

}
