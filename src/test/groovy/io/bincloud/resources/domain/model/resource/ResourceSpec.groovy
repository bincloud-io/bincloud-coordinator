package io.bincloud.resources.domain.model.resource

import io.bincloud.common.domain.model.generator.SequentialGenerator
import spock.lang.Specification

class ResourceSpec extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final String RESOURCE_FILE_NAME = "filename.txt"
	private static final String RESOURCE_RANDOM_FILE_NAME = "random-name-1234"
	private static final String OCTET_STREAM_MEDIA_TYPE = "application/octet-stream"
	private static final String INLINE_CONTENT_DISPOSITION = "inline"
	
	def "Scenario: create new resource for specified filename"() {
		given: "The resource id generator"
		SequentialGenerator<Long> idGenerator = Stub(SequentialGenerator)
		idGenerator.nextValue() >> RESOURCE_ID
		
		and: "The create resource command with specified filename"
		CreateResource resourceDetails = Stub(CreateResource)
		resourceDetails.getFileName() >> Optional.ofNullable("filename.txt")
		
		when: "The resource domain object has been created, initialized by resource details and id generator"
		Resource resource = new Resource(idGenerator, resourceDetails, {RESOURCE_RANDOM_FILE_NAME})
		
		then: "The resource id should be generated by the id generator"
		resource.getId() == RESOURCE_ID
		
		and: "The resource name should be got from resource details"
		resource.getFileName() == RESOURCE_FILE_NAME
		
		and: "The media type should be application/octet-stream"
		resource.getMediaType() == OCTET_STREAM_MEDIA_TYPE
		
		and: "The content disposition should be inline"
		resource.getContentDisposition() == INLINE_CONTENT_DISPOSITION
	}
	
	def "Scenario: create new resource for unspecified filename"() {
		given: "The resource id generator"
		SequentialGenerator<Long> idGenerator = Stub(SequentialGenerator)
		idGenerator.nextValue() >> RESOURCE_ID
		
		and: "The create resource command with specified filename"
		CreateResource resourceDetails = Stub(CreateResource)
		resourceDetails.getFileName() >> Optional.empty()
		
		when: "The resource domain object has been created, initialized by resource details and id generator"
		Resource resource = new Resource(idGenerator, resourceDetails, {RESOURCE_RANDOM_FILE_NAME})
		
		then: "The resource id should be generated by the id generator"
		resource.getId() == RESOURCE_ID
		
		and: "The resource name should be random generated"
		resource.getFileName() == RESOURCE_RANDOM_FILE_NAME
		
		and: "The media type should be application/octet-stream"
		resource.getMediaType() == OCTET_STREAM_MEDIA_TYPE
		
		and: "The content disposition should be inline"
		resource.getContentDisposition() == INLINE_CONTENT_DISPOSITION
	}
}
