package io.bincloud.common.port.adapters.web

import io.bincloud.common.domain.model.web.URLAddress
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To make operations over URL addresses simplier, as a developer I am 
	needed in separated type which will wrap URL address string and will
	provide necessary operations under URL address
""")
class URLAddressSpec extends Specification {
	def "Scenario: append tail URI part to the url address"() {
		given: "The root url address ${rootAddress}"
		URLAddress address = new URLAddress(rootAddress)
		
		and: "The uri part ${uriPart}"
		
		expect: "The URI part should be appended at the end of root uri"
		address.append(uriPart).getValue() == "http://address.url/uri/part"
		
		where:
		rootAddress           | uriPart
		"http://address.url"  | "/uri/part" 
		"http://address.url/" | "uri/part"
		"http://address.url"  | "uri/part"
		"http://address.url/"  | "/uri/part"
	}
	
	def "Scenario: append tail URI part to the url address with java formatting"() {
		given: "The root url address ${rootAddress}"
		URLAddress address = new URLAddress(rootAddress)
				
		and: "The uri part template ${uriPartTemplate}"
		
		expect: "The URI part template should be formatted and appended at the end of root uri"
		address.append(uriPartTemplate, "value").getValue() == "http://address.url/uri/part?param=value"
		
		where:
		rootAddress           | uriPartTemplate
		"http://address.url"  | "/uri/part?param=%s"
		"http://address.url/" | "uri/part?param=%s"
		"http://address.url"  | "uri/part?param=%s"
		"http://address.url/"  | "/uri/part?param=%s"
	}
	
	def "Scenario: escape url address"() {
		given: "The root url address"
		URLAddress address = new URLAddress("http://address.url")
		
		expect: "The url address should be escaped"
		address.escape().getValue() == "http%3A%2F%2Faddress.url"
	}
}
