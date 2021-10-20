package io.bcs.common.port.adapters.web


import javax.servlet.AsyncContext
import javax.servlet.http.HttpServletResponse

import io.bce.domain.BoundedContextId
import io.bce.domain.AsyncErrorsHandler.ErrorInterceptor
import io.bce.domain.errors.ApplicationException
import io.bce.domain.errors.UnexpectedErrorException
import io.bce.domain.errors.ErrorDescriptor.ErrorCode
import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import io.bce.text.TextProcessor
import io.bce.text.TextTemplates
import spock.lang.Specification

class HttpServletApplicatonExceptionHandlerSpec extends Specification {
	private HttpServletResponse response;
	private AsyncContext asyncContext;
	private TextProcessor textProcessor;

	def setup() {
		this.response = Mock(HttpServletResponse)
		this.asyncContext = Mock(AsyncContext)
		this.asyncContext.getResponse() >> response
		this.textProcessor = TextProcessor.create().withTransformer({template -> TextTemplates.createBy(String.format("%s__%s", "TRANSFORMED", template.getTemplateText()))})
	}

	def "Scenario: handle application exception"() {
		given: "The application error"
		ApplicationException error = new ApplicationException(BoundedContextId.createFor("CTX"), ErrorSeverity.BUSINESS, ErrorCode.createFor(-100L), "Smth went wrong!!!");

		and: "The configured error handler"
		ErrorInterceptor<AsyncContext, ApplicationException> errorHandler = HttpServletAppliactionExceptionHandler.applicationErrorHandler(textProcessor, 404)

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
		ApplicationException error = new ApplicationException(BoundedContextId.createFor("CTX"), ErrorSeverity.BUSINESS, ErrorCode.createFor(-1L), "Smth went wrong!!!");

		and: "The configured error handler"
		ErrorInterceptor<AsyncContext, ApplicationException> errorHandler = HttpServletAppliactionExceptionHandler.applicationErrorHandler(textProcessor, 404)

		and: "The servlet response object is wrong"
		response.sendError(_, _) >> {throw new IOException("WRONG RESPONSE")}

		when: "The application error is sent to handler"
		errorHandler.handleError(asyncContext, error)

		then: "The unexpected system behavior error should be happened"
		UnexpectedErrorException reThrownError = thrown()
		reThrownError.getContextId() == BoundedContextId.PLATFORM_CONTEXT
		reThrownError.getErrorCode() == ErrorCode.UNRECOGNIZED_ERROR_CODE

		and: "The error context should be completed"
		1 * asyncContext.complete()
	}

	def "Scenario: handle an exception by default handler"() {
		given: "The something error not related to application error"
		Exception error = new Exception("SMTH WENT WRONG")

		and: "The default error handler configured for \"BC\" bounded context"
		ErrorInterceptor<AsyncContext, Exception> errorHandler = HttpServletAppliactionExceptionHandler.defaultErrorHandler(textProcessor, BoundedContextId.createFor("BC"))

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
