package io.bce.domain.errors

import io.bce.domain.errors.ErrorDescriptor.ErrorCode
import spock.lang.Specification

class ErrorCodeSpec extends Specification {
  private static final Long ERROR_CODE_NUMBER = 12345L
  private static final String STRINGIFIED_ERROR_CODE = "12345"

  def "Scenario: wrap error code"() {
    expect: "The error code should be successfully wrapped"
    ErrorCode errorCode = ErrorCode.createFor(ERROR_CODE_NUMBER)
    errorCode.toString() == STRINGIFIED_ERROR_CODE
    errorCode.extract() == ERROR_CODE_NUMBER
  }
}
