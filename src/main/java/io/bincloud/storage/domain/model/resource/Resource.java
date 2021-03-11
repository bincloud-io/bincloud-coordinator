package io.bincloud.storage.domain.model.resource;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
public class Resource {
	@NonNull
	@EqualsAndHashCode.Include
	private Long id;
	private String fileName;
	
	public Resource(@NonNull IdGenerator idGenerator, @NonNull ResourceDetails resourceDetails) {
		super();
		this.id = idGenerator.generateId();
		this.fileName = resourceDetails.getFileName();
	}

	public interface IdGenerator {
		public Long generateId();
	}

	public interface ResourceDetails {
		public String getFileName();
	}
}
