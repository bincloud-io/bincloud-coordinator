package io.bcs.fileserver.infrastructure.api;

import io.bce.domain.errors.ErrorDescriptor;
import io.bce.domain.errors.ErrorDescriptor.ErrorCode;
import io.bcs.fileserver.domain.Constants;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * This class writes contextual headers to the web-method response.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor(staticName = "of")
public class HttpResponseContext {
  private static final String BOUNDED_CONTEXT_HEADER = "X-BC-CONTEXT";
  private static final String ERROR_CODE_HEADER = "X-BC-ERR-CODE";
  private static final String ERROR_SEVERITY_HEADER = "X-BC-ERR-SEVERITY";

  private final HttpServletResponse response;

  /**
   * Write contextual headers for fail.
   *
   * @param errorDescriptor The error descriptor
   * @param statusCode      The status code
   */
  public void writeErrorContext(ErrorDescriptor errorDescriptor, int statusCode) {
    response.setHeader(BOUNDED_CONTEXT_HEADER, errorDescriptor.getContextId().toString());
    response.setHeader(ERROR_CODE_HEADER, errorDescriptor.getErrorCode().extract().toString());
    response.setHeader(ERROR_SEVERITY_HEADER, errorDescriptor.getErrorSeverity().toString());
    response.setStatus(statusCode);
  }

  /**
   * Write contextual headers for success.
   */
  public void writeSuccessContext() {
    response.setHeader(BOUNDED_CONTEXT_HEADER, Constants.CONTEXT.toString());
    response.setHeader(ERROR_CODE_HEADER, ErrorCode.SUCCESSFUL_COMPLETED_CODE.extract().toString());
    response.setStatus(HttpServletResponse.SC_OK);
  }
}
