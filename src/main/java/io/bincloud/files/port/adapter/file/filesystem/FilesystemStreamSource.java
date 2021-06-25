package io.bincloud.files.port.adapter.file.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.bincloud.common.port.adapters.io.transfer.sources.CloseOnDisposeStreamSource;
import lombok.RequiredArgsConstructor;

public class FilesystemStreamSource extends CloseOnDisposeStreamSource {
	private long left;
	
	public FilesystemStreamSource(File file, long offset, long size, int bufferSize) throws IOException {
		super(openFileInputStream(file, offset), bufferSize);
		this.left = size;
	}

	@Override
	public void read(DestinationConnection connection) {
		super.read(new FilestreamSourceDestinationConnection(connection));
	}

	@Override
	public void dispose() {
		super.dispose();
	}
	
	private boolean isTransferringQuoteOver() {
		return this.left == 0;
	}
	
	private long getAvailableBytesCountOutOfSuggestedAmount(long suggestedAmount) {
		if (left > suggestedAmount) {
			return suggestedAmount;
		}
		return left;
	}
	
	private void decreaseTransferredSize(long transferredSize) {
		this.left -= transferredSize;
	}
	
	private static final FileInputStream openFileInputStream(File file, long offset) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(file);
		fileInputStream.skip(offset);
		return fileInputStream;
	}
	
	@RequiredArgsConstructor
	private class FilestreamSourceDestinationConnection implements DestinationConnection {
		private final DestinationConnection connection;

		@Override
		public void submit(ByteBuffer buffer, Long count) {
			if (!isTransferringQuoteOver()) {
				long availableAmount = getAvailableBytesCountOutOfSuggestedAmount(count);
				this.submitAvailableBytes(buffer, availableAmount);
			} else {				
				complete();
			}
		}

		@Override
		public void complete() {
			connection.complete();
		}
		
		private void submitAvailableBytes(ByteBuffer buffer, Long availableAmount) {
			decreaseTransferredSize(availableAmount);
			connection.submit(buffer, availableAmount);
			
		}
	}
}
