package io.bincloud.storage.domain.model.resource;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Resource {
	@NonNull
	@EqualsAndHashCode.Include
	private Long id;
	@NonNull
	private String fileName;
	
	public Resource(@NonNull SequentialGenerator<Long> idGenerator, @NonNull ResourceDetails resourceDetails) {
		super();
		this.id = idGenerator.nextValue();
		this.fileName = resourceDetails.getFileName();
	}

	public interface ResourceDetails {
		public String getFileName();
	}
}
