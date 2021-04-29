package io.bincloud.common.components.io

import io.bincloud.common.io.transfer.CompletionCallback
import io.bincloud.common.io.transfer.CompletionCallbackWrapper
import spock.lang.Specification

class CompletionCallbackWrapperSpec extends Specification {
	private CompletionCallback original;
	def setup() {
		this.original = Mock(CompletionCallback)
	}
	
	def "Scenario: decorate success"() {
		given: "Wrapped original callback"
		def wrapped = new CompletionCallbackWrapper(original)
		
		when: "The success method is called"
		wrapped.onSuccess()
		
		then: "The behavior should be delegated to original"
		1 * original.onSuccess()
	}
	
	def "Scenario: decorate error"() {
		given: "Wrapped original callback"
		def wrapped = new CompletionCallbackWrapper(original)
		Exception error = new Exception()
		
		when: "The success method is called"
		wrapped.onError(error)
		
		then: "The behavior should be delegated to original"
		1 * original.onError(error)
	}
}
