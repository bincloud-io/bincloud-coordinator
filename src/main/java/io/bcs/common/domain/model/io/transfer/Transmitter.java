package io.bcs.common.domain.model.io.transfer;

import io.bcs.common.domain.model.io.transfer.DestinationPoint.SourceConnection;
import io.bcs.common.domain.model.io.transfer.SourcePoint.DestinationConnection;

/**
 * This interface describes the data transmitter functions, allowing
 * start scheduled data transmission. 
 * 
 * 
 * @author Dmitry Mikhaylenko
 *
 */
public interface Transmitter extends SourceConnection, DestinationConnection {
	/**
	 * Start data transmission
	 */
	public void start();
}