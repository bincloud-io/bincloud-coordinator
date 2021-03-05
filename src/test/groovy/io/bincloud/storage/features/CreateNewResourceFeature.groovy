package io.bincloud.storage.features

import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	In order to identify a stored file in the object storage,
	As a user, I want to create a resource that will represent the corresponding file in the storage
""")
class CreateNewResourceFeature extends Specification {
	def "Scenario: create resource new resource"() {
		given: "Resource hasn't been created yet"
		and: "The request parameters are valid"
		when: "The create new resource operation has been called"
		then: "The new resource has been created in the store"
		and: "The generated identifier has been assigned to resource"
		and: "The "
			1==2
	}
	
	def "Scenario: try to create resource with invalid request parameters"() {
		given: "Resource hasn't been created yet"
		and: "The resource creation command is invalid"
		when: "The create new resource operation has been called"
		then: "The validation error occurred"
			1==2
	}
}
