package io.bincloud.common.port.adapters.io.transfer.sources;

import io.bincloud.common.domain.model.error.MustNeverBeHappenedError;
import io.bincloud.common.domain.model.io.transfer.CloseableSourcePoint;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
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
