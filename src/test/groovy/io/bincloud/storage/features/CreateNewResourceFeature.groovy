package io.bincloud.storage.features

import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	In order to identify a stored file in the object storage,
	As a user, I want to create a resource that will represent the corresponding file in the storage
""")
class CreateNewResourceFeature extends Specification {
	def "Scenario: create resource which doesn't exist and input command is valid"() {
		given: "Resource doesn't exist in repository"
		and: "The valid input command"
		when: "The create new resource command executed"
		then: "The resource has been saved in the store"
		and: "The saved resource id the same as generated id"
		and: "The saved resource name the same as in command"
			1==2
	}
	
	def "Scenario: create resource which doesn't exist but input command is invalid"() {
		given: "Resource doesn't exist in repository"
		and: "The invalid input command"
		when: "The create new resource command executed"
		then: "The error occurred notifying about invalid input command"
			1==2
	}
	
	def "Scenario: create resource which already exists but command is valid"() {
		given: "Resource exists in repository"
		and: "The valid input command"
		when: "The create new resource command executed"
		then: "The error occurred notifying about existing resource"
			1==2
	}
}
