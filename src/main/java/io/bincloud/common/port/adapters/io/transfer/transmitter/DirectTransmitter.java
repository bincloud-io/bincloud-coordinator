package io.bincloud.common.port.adapters.io.transfer.transmitter;

import java.nio.ByteBuffer;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.common.domain.model.io.transfer.Transmitter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectTransmitter implements Transmitter {
	private final SourcePoint source;
	private final DestinationPoint destination;
	private final CompletionCallback  completionCallback;
	
	@Override
	public void submit(ByteBuffer buffer, Long count) {
		destination.write(this, buffer, count);				
	}

	@Override
	public void receive() {
		source.read(this);
	}
	

	@Override
	public void start() {
		try {
			receive();
		} catch (Exception error) {
			fail(error);
		}
	}

	@Override
	public void complete() {
		dispose();
		completionCallback.onSuccess();
	}
	
	private void fail(Exception error) {
		dispose();
		completionCallback.onError(error);
	}
	
	private void dispose() {
		source.dispose();
		destination.dispose();
	}
}
