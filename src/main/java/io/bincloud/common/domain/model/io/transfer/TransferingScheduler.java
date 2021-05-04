package io.bincloud.common.domain.model.io.transfer;

public interface TransferingScheduler {
	public Transmitter schedule(SourcePoint source, DestinationPoint destination,
			CompletionCallback completionCallback);
}
