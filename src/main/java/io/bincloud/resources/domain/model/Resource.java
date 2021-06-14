package io.bincloud.resources.domain.model;

import java.util.Optional;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Resource {
	@EqualsAndHashCode.Include
	private Long id;
	private String fileName;
	
	public Resource(
			SequentialGenerator<Long> idGenerator, 
			ResourceDetails resourceDetails, 
			SequentialGenerator<String> randomFileNameGenerator) {
		super();
		this.id = idGenerator.nextValue();
		this.fileName = resourceDetails.getFileName().orElse(randomFileNameGenerator.nextValue());
	}

	public interface ResourceDetails {
		public Optional<String> getFileName();
	}
}
