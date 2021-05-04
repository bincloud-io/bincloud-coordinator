package io.bincloud.storage.domain.model.resource

import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.storage.domain.model.resource.Resource.ResourceDetails
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To represent business object \"Resource\" in the source code, 
	as a developer I'm needed in the component, which will do
	this. According to business domain, I want a domain object which 
	will encapsulate domain object state and logic which automates this
	one.
""")
class ResourceSpec extends Specification {
	def "Scenario: create new resource"() {
		given: "The resource id generator"
		SequentialGenerator<Long> idGenerator = Stub(SequentialGenerator)
		idGenerator.nextValue() >> 1L
		
		and: "The resource details"
		ResourceDetails resourceDetails = Stub(ResourceDetails)
		resourceDetails.getFileName() >> "filename.txt"
		
		when: "The resource domain object has been created, initialized by resource details and id generator"
		Resource resource = new Resource(idGenerator, resourceDetails)
		
		then: "The resource id should be generated by the id generator"
		resource.getId() == 1L
		
		and: "The resource name should be got from resource details"
		resource.getFileName() == resourceDetails.getFileName()
	}
}
