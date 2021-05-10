package io.bincloud.storage.port.adapter.file;

import java.io.File;
import java.io.IOException;

import io.bincloud.common.domain.model.error.MustNeverBeHappenedError;
import io.bincloud.common.domain.model.error.UnexpectedSystemBehaviorException;
import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.storage.domain.model.file.FileManagementException;
import io.bincloud.storage.domain.model.file.FilesystemAccessor;
import lombok.NonNull;
import lombok.SneakyThrows;

public class BlockedFileSystemAccessor implements FilesystemAccessor {
	private final File rootFolder;
	private final int bufferSize;

	public BlockedFileSystemAccessor(String filesFolderPath, int bufferSize) {
		super();
		this.rootFolder = this.createRootDirectoryFile(filesFolderPath);
		this.bufferSize = bufferSize;
	}

	@Override
	@SneakyThrows
	public void createFile(String fileName) {
		File file = new File(this.rootFolder, fileName);
		if (!file.createNewFile()) {
			throw new MustNeverBeHappenedError("Existing file mustn't created twice");
		}
	}

	@Override
	public Long getFileSize(String fileName) {
		File file = new File(this.rootFolder, fileName);
		if (!file.exists()) {
			throw new MustNeverBeHappenedError("It is allowable to get size for existing files only");
		}
		return file.length();
	}

	@Override
	public SourcePoint getAccessOnRead(String fileName, @NonNull Long offset, @NonNull Long size) {
		try {
			return new FilesystemStreamSource(new File(this.rootFolder, fileName), offset, size, bufferSize);
		} catch (IOException error) {
			throw new UnexpectedSystemBehaviorException(FileManagementException.CONTEXT, error);
		}
	}

	@Override
	public DestinationPoint getAccessOnWrite(String fileName) {
		try {
			return new FilesystemStreamDestination(new File(this.rootFolder, fileName));
		} catch (IOException error) {
			throw new UnexpectedSystemBehaviorException(FileManagementException.CONTEXT, error);
		}
	}

	private File createRootDirectoryFile(String filesFolderPath) {
		File file = new File(filesFolderPath);
		checkRootFolderExistence(file);
		checkRootFolderFileType(file);
		return file;
	}

	private void checkRootFolderFileType(File file) throws MustNeverBeHappenedError {
		if (!file.isDirectory()) {
			throw new MustNeverBeHappenedError("Specified root folder path isn't directory");
		}
	}

	private void checkRootFolderExistence(File file) throws MustNeverBeHappenedError {
		if (!file.exists()) {
			throw new MustNeverBeHappenedError("The root directory must be existed");
		}
	}
}
