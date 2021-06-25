package io.bincloud.common.port.adapters.io.transfer.destinations;

import java.io.OutputStream;

public class CloseOnDisposeStreamDestination extends CloseOnDisposeDestinationPoint {
	public CloseOnDisposeStreamDestination(OutputStream outputStream) {
		super(new StreamDestination(outputStream));
	}
}
