package io.bcs.common.port.adapters.io.transfer.sources;

import java.io.InputStream;

public class CloseOnDisposeStreamSource extends CloseOnDisposeSourcePoint {
	public CloseOnDisposeStreamSource(InputStream consumerStream, int bufferSize) {
		super(new StreamSource(consumerStream, bufferSize));
	}
}
