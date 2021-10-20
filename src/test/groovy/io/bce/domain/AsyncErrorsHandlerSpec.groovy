package io.bce.domain

import io.bce.domain.AsyncErrorsHandler.ErrorInterceptor
import io.bce.domain.errors.UnexpectedErrorException
import spock.lang.Specification

class AsyncErrorsHandlerSpec extends Specification {
	def "Scenario: receive wrong context type"() {
		given: "The created for specified context type errors handler"
		AsyncErrorsHandler<Context> errorHandler = AsyncErrorsHandler.createFor(Context)
		
		when: "The wrong context type has been passed to the error handling method"
		errorHandler.handleError(new Object(), new Exception())
		
		then: "The illegal argument exception should be received"
		thrown(IllegalArgumentException)
	}
	
	def "Scenario: receive unknown exception type with unregistered default handler"() {
		given: "The created for specified context type errors handler"
		AsyncErrorsHandler<Context> errorHandler = AsyncErrorsHandler.createFor(Context)
		
		and: "The error handling context"
		Context context = new Context()
		
		and: "The exception"
		Exception error = new Exception()
		
		when: "The unregistered error and context have been passed by the error handler"
		errorHandler.handleError(context, error)
		
		then: "The received error should be rethrown"
		Exception thrownException = thrown(UnexpectedErrorException)
		error.is(thrownException.cause)
	}
	
	def "Scenario: receive unknown exception type with registered default handler"() {
		given: "The created for specified context type errors handler"
		AsyncErrorsHandler<Context> errorHandler = AsyncErrorsHandler.createFor(Context)
		
		and: "The registered default error handler"
		ErrorInterceptor<Context, Exception> defaultErrorHandler = Mock(ErrorInterceptor)
		errorHandler = errorHandler.registerDefaultHandler(defaultErrorHandler)
		 
		and: "The error handling context"
		Context context = new Context()
		
		and: "The exception"
		Exception error = new Exception()
		
		when: "The unregistered error and context have been passed by the error handler"
		errorHandler.handleError(context, error)
		
		then: "The error and context should be received by registered handler"
		1 * defaultErrorHandler.handleError(context, error);
	}
	
	def "Scenario: receive exception with registered type"() {
		given: "The created for specified context type errors handler"
		AsyncErrorsHandler<Context> errorHandler = AsyncErrorsHandler.createFor(Context)
		
		and: "The registered error handler"
		ErrorInterceptor<Context, Exception> specificErrorHandler = Mock(ErrorInterceptor)
		errorHandler = errorHandler.registerHandler(Exception, specificErrorHandler)
		 
		and: "The error handling context"
		Context context = new Context()
		
		and: "The exception"
		Exception error = new Exception()
		
		when: "The unregistered error and context have been passed by the error handler"
		errorHandler.handleError(context, error)
		
		then: "The error and context should be received by registered handler"
		1 * specificErrorHandler.handleError(context, error);
	}
	
	class Context {
		
	}
}
