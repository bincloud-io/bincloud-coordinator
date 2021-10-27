package io.bcs.storage.port.adapter.file.filesystem;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import io.bce.MustNeverBeHappenedError;
import io.bce.domain.errors.UnexpectedErrorException;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.interaction.streaming.binary.BinaryChunk.BinaryChunkWriter;
import io.bce.interaction.streaming.binary.BinaryDestination;
import io.bcs.common.domain.model.io.transfer.DestinationPoint;
import io.bcs.common.domain.model.io.transfer.SourcePoint;
import io.bcs.storage.domain.model.Constants;
import io.bcs.storage.domain.model.FilesystemAccessor;
import io.bcs.storage.domain.model.states.FileManagementException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class BlockedFileSystemAccessor implements FilesystemAccessor {
	private final File rootFolder;
	private final int bufferSize;

	public BlockedFileSystemAccessor(String filesFolderPath, int bufferSize) {
		super();
		this.rootFolder = this.createRootDirectoryFile(filesFolderPath);
		this.bufferSize = bufferSize;
	}

	@Override
	public void createFile(String fileName) {
		File file = new File(this.rootFolder, fileName);
		try {
			if (!file.createNewFile()) {
				throw new MustNeverBeHappenedError("Existing file mustn't created twice");
			}
		} catch (IOException error) {
			throw new UnexpectedErrorException(Constants.CONTEXT, error);
		}
	}
	
	@Override
	public BinaryDestination getAccessOnWrite(String fileName) {
		File file = new File(this.rootFolder, fileName);
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			return new BlockingFileSystemBinarySource(fileInputStream);
		} catch (IOException ioError) {
			throw new UnexpectedErrorException(Constants.CONTEXT, ioError);
		}
	}

	@Override
	public SourcePoint getAccessOnRead(String fileName, Long offset, Long size) {
		try {
			return new FilesystemStreamSource(new File(this.rootFolder, fileName), offset, size, bufferSize);
		} catch (IOException error) {
			throw new UnexpectedErrorException(FileManagementException.CONTEXT, error);
		}
	}

	@Override
	public DestinationPoint getAccessOnWrite(String fileName, Long contentSize) {
		try {
			return new FilesystemStreamDestination(new File(this.rootFolder, fileName));
		} catch (IOException error) {
			throw new UnexpectedErrorException(FileManagementException.CONTEXT, error);
		}
	}

	@Override
	public void removeFile(String fileName) {
		File file = new File(this.rootFolder, fileName);
		if (!file.delete()) {
			throw new MustNeverBeHappenedError("Existing file mustn't created twice");
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
	
	private static class BlockingFileSystemBinarySource extends BinaryDestination {
		private Closeable fileCloser;
		
		public BlockingFileSystemBinarySource(@NonNull FileInputStream fileInputStream) {
			super(new BlockingFileReader(fileInputStream));
			this.fileCloser = fileInputStream;
		}

		@Override
		public void release() {
			try {
				this.fileCloser.close();
			} catch (IOException ioError) {
				throw new MustNeverBeHappenedError(ioError);
			}
		}
	
	}
	
	@RequiredArgsConstructor
	private static class BlockingFileReader implements BinaryChunkWriter {
		@NonNull
		private final FileInputStream fileInputStream;
		
		@Override
		public void writeChunk(BinaryChunk chunk) {
			// TODO Auto-generated method stub
			
		}
		
	}
}