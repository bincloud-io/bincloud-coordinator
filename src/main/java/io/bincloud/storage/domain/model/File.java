package io.bincloud.storage.domain.model;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class File {
	@EqualsAndHashCode.Include
	private final UUID fileId;
	
	public File(@NonNull IdGenerator idGenerator) {
		super();
		this.fileId = idGenerator.generateId();
	}

	public interface IdGenerator {
		public UUID generateId();
	}
}
