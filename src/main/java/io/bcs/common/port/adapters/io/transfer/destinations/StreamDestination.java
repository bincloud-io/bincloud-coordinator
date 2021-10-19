package io.bcs.common.port.adapters.io.transfer.destinations;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import io.bcs.common.domain.model.io.transfer.CloseableDestinationPoint;
import io.bcs.common.domain.model.io.transfer.DataTransferingException;

public class StreamDestination implements CloseableDestinationPoint {
	private final OutputStream outputStream;

	public StreamDestination(OutputStream outputStream) {
		super();
		this.outputStream = outputStream;
	}

	@Override
	public void write(SourceConnection connection, ByteBuffer buffer, Long count) {
		try {
			outputStream.write(buffer.array(), 0, count.intValue());
			connection.receive();
		} catch (IOException error) {
			throw new DataTransferingException(error.getMessage());
		}
	}

	@Override
	public void dispose() {
	}
	
	@Override
	public void close() throws Exception {
		outputStream.close();
	}
}
