package io.bcs.common.port.adapters.web

import static io.bcs.common.domain.model.error.ApplicationException.Severity.BUSINESS

import javax.servlet.AsyncContext
import javax.servlet.http.HttpServletResponse

import io.bcs.common.domain.model.error.ApplicationException
import io.bcs.common.domain.model.error.UnexpectedSystemBehaviorException
import io.bcs.common.domain.model.error.ApplicationException.Severity
import io.bcs.common.domain.model.error.AsyncErrorsHandler.ErrorInterceptor
import io.bcs.common.domain.model.message.MessageProcessor
import io.bcs.common.domain.model.message.templates.TextMessageTemplate
import io.bcs.common.port.adapters.web.HttpServletAppliactionExceptionHandler
import spock.lang.Specification

class HttpServletApplicatonExceptionHandlerSpec extends Specification {
	private HttpServletResponse response;
	private AsyncContext asyncContext;
	private MessageProcessor messageProcessor;

	def setup() {
		this.response = Mock(HttpServletResponse)
		this.asyncContext = Mock(AsyncContext)
		this.asyncContext.getResponse() >> response
		this.messageProcessor = new MessageProcessor().configure()
				.withTransformation({messageTemplate ->
					new TextMessageTemplate(String.format("%s__%s", "TRANSFORMED", messageTemplate.getText()))
				})
				.apply();
	}

	def "Scenario: handle application exception"() {
		given: "The application error"
		ApplicationException error = new ApplicationException(BUSINESS, "CTX", -100L, "Smth went wrong!!!");

		and: "The configured error handler"
		ErrorInterceptor<AsyncContext, ApplicationException> errorHandler = HttpServletAppliactionExceptionHandler.applicationErrorHandler(messageProcessor, 404)

		when: "The application error is sent to handler"
		errorHandler.handleError(asyncContext, error)

		then: "The error bounded context header should be set"
		1 * response.setHeader(HttpServletAppliactionExceptionHandler.ERROR_BOUNDED_CONTEXT_HTTP_HEADER, "CTX")

		and: "The error code header should be set"
		1 * response.setHeader(HttpServletAppliactionExceptionHandler.ERROR_CODE_HTTP_HEADER, "-100")

		and: "The error severity header should be set"
		1 * response.setHeader(HttpServletAppliactionExceptionHandler.ERROR_SEVERITY_HEADER, "BUSINESS")

		and: "The error with specified code and transformed message should be sent"
		1 * response.sendError(404, "TRANSFORMED__ERROR.CTX.-100")

		and: "The error context should be completed"
		1 * asyncContext.complete()
	}

	def "Scenario: handle application exception with wrong servlet response"() {
		given: "The application error"
		ApplicationException error = new ApplicationException(BUSINESS, "CTX", -1L, "Smth went wrong!!!");

		and: "The configured error handler"
		ErrorInterceptor<AsyncContext, ApplicationException> errorHandler = HttpServletAppliactionExceptionHandler.applicationErrorHandler(messageProcessor, 404)

		and: "The servlet response object is wrong"
		response.sendError(_, _) >> {throw new IOException("WRONG RESPONSE")}

		when: "The application error is sent to handler"
		errorHandler.handleError(asyncContext, error)

		then: "The unexpected system behavior error should be happened"
		UnexpectedSystemBehaviorException reThrownError = thrown()
		reThrownError.getContext() == "GLOBAL"
		reThrownError.getErrorCode() == -1L

		and: "The error context should be completed"
		1 * asyncContext.complete()
	}

	def "Scenario: handle an exception by default handler"() {
		given: "The something error not related to application error"
		Exception error = new Exception("SMTH WENT WRONG")

		and: "The default error handler configured for \"BC\" bounded context"
		ErrorInterceptor<AsyncContext, Exception> errorHandler = HttpServletAppliactionExceptionHandler.defaultErrorHandler(messageProcessor, "BC")

		when: "The error is sent to handler"
		errorHandler.handleError(asyncContext, error)

		then: "The \"BC\" bounded context header should be set"
		1 * response.setHeader(HttpServletAppliactionExceptionHandler.ERROR_BOUNDED_CONTEXT_HTTP_HEADER, "BC")

		and: "The error code header should be set"
		1 * response.setHeader(HttpServletAppliactionExceptionHandler.ERROR_CODE_HTTP_HEADER, "-1")

		and: "The error severity header should be set"
		1 * response.setHeader(HttpServletAppliactionExceptionHandler.ERROR_SEVERITY_HEADER, "INCIDENT")

		and: "The error with specified code and transformed message should be sent"
		1 * response.sendError(500, "TRANSFORMED__ERROR.BC.-1")

		and: "The error context should be completed"
		1 * asyncContext.complete()
	}
}
