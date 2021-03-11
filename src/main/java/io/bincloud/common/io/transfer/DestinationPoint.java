package io.bincloud.common.io.transfer;

import java.nio.ByteBuffer;

public interface DestinationPoint {
	public void write(SourceConnection connection, ByteBuffer buffer, Long count);
	public void dispose();
	
	public interface SourceConnection {
		public void receive();
	}	
}