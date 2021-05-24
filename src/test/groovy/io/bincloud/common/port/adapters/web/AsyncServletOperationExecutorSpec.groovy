package io.bincloud.common.port.adapters.web

import java.util.function.Consumer

import javax.servlet.AsyncContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import spock.lang.Specification



class AsyncServletOperationExecutorSpec extends Specification {
	def "Scenario: call async operation for servlet request"() {
		given: "The http servlet request"
		HttpServletRequest request = Stub(HttpServletRequest)
		
		and: "The http servlet response"
		HttpServletResponse response = Stub(HttpServletResponse)
		
		and: "The http servlet request supports async operations"
		AsyncContext asyncContext = Mock(AsyncContext)
		request.startAsync(request, response) >> asyncContext
		
		and: "The async operation runner"
		Consumer<AsyncContext> operationRunner = Mock(Consumer)
		
		when: "The async operation has been executed"
		AsyncServletOperationExecutor asyncExecutor = new AsyncServletOperationExecutor(request, response)
		asyncExecutor.execute(operationRunner)
		
		then: "The consumer should receive async context for started async operation" 
			1 * operationRunner.accept(asyncContext)
	}
	
}
