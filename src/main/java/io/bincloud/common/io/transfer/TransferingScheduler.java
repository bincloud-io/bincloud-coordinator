package io.bincloud.common.io.transfer;

public interface TransferingScheduler {
	public Transmitter schedule(SourcePoint source, DestinationPoint destination,
			CompletionCallback completionCallback);
}
