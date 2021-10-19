package io.bcs.common.port.adapters.io.transfer.transmitter;

import io.bcs.common.domain.model.io.transfer.CompletionCallback;
import io.bcs.common.domain.model.io.transfer.DestinationPoint;
import io.bcs.common.domain.model.io.transfer.SourcePoint;
import io.bcs.common.domain.model.io.transfer.TransferingScheduler;
import io.bcs.common.domain.model.io.transfer.Transmitter;

public class DirectTransferingScheduler implements TransferingScheduler {
	@Override
	public Transmitter schedule(SourcePoint source, DestinationPoint destination,
			CompletionCallback completionCallback) {
		return new DirectTransmitter(source, destination, completionCallback);
	}
}
