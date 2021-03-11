package io.bincloud.common.io.transfer;

import java.nio.ByteBuffer;

public interface SourcePoint {
	public void read(DestinationConnection connection);
	public void dispose();
	
	public interface DestinationConnection {
		public void submit(ByteBuffer buffer, Long count);
		
		public void complete();
	}
}
