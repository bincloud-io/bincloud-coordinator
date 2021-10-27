package io.bcs.common.port.adapters.io.transfer.sources;

import io.bce.MustNeverBeHappenedError;
import io.bcs.common.domain.model.io.transfer.CloseableSourcePoint;
import io.bcs.common.domain.model.io.transfer.SourcePoint;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CloseOnDisposeSourcePoint implements SourcePoint {
	private final CloseableSourcePoint sourcePoint;

	@Override
	public void read(DestinationConnection connection) {
		sourcePoint.read(connection);
	}

	@Override
	public void dispose() {
		sourcePoint.dispose();
		closePoint();
	}
	
	private void closePoint() {
		try {
			sourcePoint.close();
		} catch (Exception error) {
			throw new MustNeverBeHappenedError(error);
		}
	}
}