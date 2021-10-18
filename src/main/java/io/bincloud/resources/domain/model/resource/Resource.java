package io.bincloud.resources.domain.model.resource;

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
	public static final String APPLICATION_OCTET_STREAM_MEDIA_TYPE = "application/octet-stream";
	public static final String DEFAULT_CONTENT_DISPOSITION = "inline";
	
	@EqualsAndHashCode.Include
	private Long id;
	private String fileName;
	
	public Resource(
			SequentialGenerator<Long> idGenerator, 
			CreateResource resourceDetails, 
			SequentialGenerator<String> randomFileNameGenerator) {
		super();
		this.id = idGenerator.nextValue();
		this.fileName = resourceDetails.getFileName().orElse(randomFileNameGenerator.nextValue());
	}
	
	public String getMediaType() {
		return APPLICATION_OCTET_STREAM_MEDIA_TYPE;
	}
	
	public String getContentDisposition() {
		return DEFAULT_CONTENT_DISPOSITION;
	}
}
