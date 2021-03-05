package io.bincloud.storage.domain.model.resource;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
public class Resource {
	@NonNull
	@EqualsAndHashCode.Include
	private Long id;
	
	public Resource(@NonNull IdGenerator idGenerator) {
		super();
		this.id = idGenerator.generateId();
	}
	
	public interface IdGenerator {
		public Long generateId();
	}
}
