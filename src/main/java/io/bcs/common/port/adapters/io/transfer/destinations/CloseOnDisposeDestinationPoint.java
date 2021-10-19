package io.bcs.common.port.adapters.io.transfer.destinations;

import java.nio.ByteBuffer;

import io.bcs.common.domain.model.error.MustNeverBeHappenedError;
import io.bcs.common.domain.model.io.transfer.CloseableDestinationPoint;
import io.bcs.common.domain.model.io.transfer.DestinationPoint;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CloseOnDisposeDestinationPoint implements DestinationPoint {
	private final CloseableDestinationPoint destinationPoint;

	@Override
	public void write(SourceConnection connection, ByteBuffer buffer, Long count) {
		destinationPoint.write(connection, buffer, count);
	}

	@Override
	public void dispose() {
		destinationPoint.dispose();
		closePoint();
	}
	
	private void closePoint() {
		try {
			destinationPoint.close();
		} catch (Exception error) {
			throw new MustNeverBeHappenedError(error);
		}
	}
}
