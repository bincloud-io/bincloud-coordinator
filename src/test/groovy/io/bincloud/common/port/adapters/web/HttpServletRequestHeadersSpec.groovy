package io.bincloud.common.port.adapters.web

import javax.servlet.http.HttpServletRequest

import spock.lang.Specification

class HttpServletRequestHeadersSpec extends Specification {
	def "Scenario: init from servlet request"() {
		given: "The servlet request"
		HttpServletRequest servletRequest = Stub(HttpServletRequest)
		
		and: "The servlet request contains headers"
		servletRequest.getHeaderNames() >> Collections.enumeration(["header-1", "header-2"])
		servletRequest.getHeaders("header-1") >> Collections.enumeration(["1", "2", "3"])
		servletRequest.getHeaders("header-2") >> Collections.enumeration(["A", "B", "C"])
		
		expect: "The servlet request headers should be correctly initialized"
		HttpServletRequestHeaders headers = new HttpServletRequestHeaders(servletRequest)
		
		and: "The headers named \"header-1\" might be obtained by name"
		headers.getIntValues("header-1") == [1, 2, 3]
		
		and: "The headers named \"header-2\" might be obtained by name"
		headers.getValues("header-2") == ["A", "B", "C"]
	}
}
