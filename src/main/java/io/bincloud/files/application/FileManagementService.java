package io.bincloud.files.application;

import java.util.Optional;
import java.util.function.Consumer;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.CompletionCallbackWrapper;
import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.common.domain.model.io.transfer.TransferingScheduler;
import io.bincloud.files.domain.model.File;
import io.bincloud.files.domain.model.FileDescriptor;
import io.bincloud.files.domain.model.FileDownloadingContext;
import io.bincloud.files.domain.model.FileRepository;
import io.bincloud.files.domain.model.FileUploadingContext;
import io.bincloud.files.domain.model.FilesystemAccessor;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.files.domain.model.errors.FileNotExistException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileManagementService implements FileStorage {
	private final SequentialGenerator<String> idGenerator;
	private final FileRepository fileRepository;
	private final FilesystemAccessor filesystemAccessor;
	private final TransferingScheduler transferingScheduler;

	@Override
	public String createNewFile() {
		File file = new File(idGenerator);
		file.createFile(filesystemAccessor);
		fileRepository.save(file);
		return file.getFileId();
	}

	@Override
	public Optional<FileDescriptor> getFileDescriptor(String fileId) {
		return fileRepository.findById(fileId).map(file -> file);
	}

	@Override
	public void uploadFile(String fileId, SourcePoint source, CompletionCallback callback) {
		consumeExistingFileAsync(fileId, callback, file -> {
			file.uploadFile(new FileUploadingContext(source, transferingScheduler, filesystemAccessor,
					new CompletionCallbackWrapper(callback) {
						@Override
						public void onSuccess() {
							file.startDistribution(filesystemAccessor);
							fileRepository.save(file);
							super.onSuccess();
						}
					}));
		});
	}

	@Override
	public void downloadFile(String fileId, DestinationPoint destination, CompletionCallback callback) {
		consumeExistingFileAsync(fileId, callback,
				file -> file.downloadFile(createDownloadingContext(destination, callback)));
	}

	@Override
	public void downloadFileRange(String fileId, DestinationPoint destination, CompletionCallback callback, Long offset,
			Long size) {
		consumeExistingFileAsync(fileId, callback,
				file -> file.downloadFileRange(createDownloadingContext(destination, callback), offset, size));
	}

	private FileDownloadingContext createDownloadingContext(DestinationPoint destination, CompletionCallback callback) {
		return new FileDownloadingContext(destination, transferingScheduler, filesystemAccessor, callback);
	}

	private void consumeExistingFileAsync(String fileId, CompletionCallback callback, Consumer<File> consumer) {
		Optional<File> file = fileRepository.findById(fileId);
		if (!file.isPresent()) {
			callback.onError(new FileNotExistException());
		} else {
			consumer.accept(file.get());
		}
	}

	@Override
	public void disposeFile(String fileId) {
		File file = fileRepository.findById(fileId).orElseThrow(FileNotExistException::new);
		file.dispose();
		fileRepository.save(file);
	}

}