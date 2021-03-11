package io.bincloud.common.io.transfer.transmitter;

import io.bincloud.common.io.transfer.CompletionCallback;
import io.bincloud.common.io.transfer.DestinationPoint;
import io.bincloud.common.io.transfer.SourcePoint;
import io.bincloud.common.io.transfer.TransferingScheduler;
import io.bincloud.common.io.transfer.Transmitter;

public class DirectTransferingScheduler implements TransferingScheduler {
	@Override
	public Transmitter schedule(SourcePoint source, DestinationPoint destination,
			CompletionCallback completionCallback) {
		return new DirectTransmitter(source, destination, completionCallback);
	}
}
