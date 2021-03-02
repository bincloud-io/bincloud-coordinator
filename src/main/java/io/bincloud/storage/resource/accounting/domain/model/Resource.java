package io.bincloud.storage.resource.accounting.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class Resource {
	@EqualsAndHashCode.Include
	private final Long id; 
	
	public Resource(@NonNull IdGenerator idGenerator) {
		super();
		this.id = idGenerator.generateId();
	}
	
	public interface IdGenerator {
		public Long generateId();
	}
}
