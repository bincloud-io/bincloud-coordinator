package io.bce.interaction.streaming.binary;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class BinaryChunk {
	public static final BinaryChunk EMPTY = new BinaryChunk(new byte[0]);
	
	private final byte[] body;
	
	public byte[] getBody() {
		return body;
	}
	
	public int getSize() {
		return body.length;
	}
	
	public boolean isEmpty() {
		return getSize() == 0;
	}
	
	public interface BinaryChunkReader {
		public BinaryChunk readChunk();
	}
	
	public interface BinaryChunkWriter {
		public void writeChunk(BinaryChunk chunk);
	}
}
