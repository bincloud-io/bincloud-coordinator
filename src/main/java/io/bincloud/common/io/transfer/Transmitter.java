package io.bincloud.common.io.transfer;

import io.bincloud.common.io.transfer.DestinationPoint.SourceConnection;
import io.bincloud.common.io.transfer.SourcePoint.DestinationConnection;

public interface Transmitter extends SourceConnection, DestinationConnection {
	public void start();
}
