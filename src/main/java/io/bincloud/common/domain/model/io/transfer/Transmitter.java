package io.bincloud.common.domain.model.io.transfer;

import io.bincloud.common.domain.model.io.transfer.DestinationPoint.SourceConnection;
import io.bincloud.common.domain.model.io.transfer.SourcePoint.DestinationConnection;

public interface Transmitter extends SourceConnection, DestinationConnection {
	public void start();
}
