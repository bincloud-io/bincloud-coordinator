package io.bcs.common.port.adapters.io.transfer.sources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import io.bcs.common.domain.model.io.transfer.CloseableSourcePoint;
import io.bcs.common.domain.model.io.transfer.DataTransferingException;


public class StreamSource implements CloseableSourcePoint {
	
	private final InputStream consumerStream;
	private final byte[] consumerBuffer;
	private final int bufferSize;

	public StreamSource(InputStream consumerStream, int bufferSize) {
		super();
		this.consumerStream = consumerStream;
		this.consumerBuffer = new byte[bufferSize];
		this.bufferSize = bufferSize;
	}

	@Override
	public void read(DestinationConnection connection) {
		try {
			transferToDestination(connection);
		} catch (IOException error) {
			throw new DataTransferingException(error.getMessage());
		}
	}

	private void transferToDestination(DestinationConnection connection) throws IOException {
		int readCount = consumerStream.read(consumerBuffer, 0, bufferSize);
		if (readCount != -1) {
			submit(connection, readCount);				
		} else {
			complete(connection);
		}
	}

	private void submit(DestinationConnection connection, int readCount) {
		connection.submit(ByteBuffer.wrap(consumerBuffer), (long) readCount);
	}
	
	private void complete(DestinationConnection connection) {
		connection.complete();
	}

	@Override
	public void dispose() {
	}
	
	@Override
	public void close() throws Exception {
		consumerStream.close();
	} 
}
