package io.bcs.common.port.adapters.web


import io.bcs.common.domain.model.web.KeyValueParameters
import spock.lang.Specification

class KeyValueParametersSpec extends Specification {
	private static final String STRING_HEADERS_PARAMETER = "string-values"
	private static final String BYTE_HEADERS_PARAMETER = "byte-values"
	private static final String SHORT_HEADERS_PARAMETER = "short-values"
	private static final String INTEGER_HEADERS_PARAMETER = "int-values"
	private static final String LONG_HEADERS_PARAMETER = "long-values"
	private static final String FLOAT_HEADERS_PARAMETER = "float-values"
	private static final String DOUBLE_HEADERS_PARAMETER = "double-values"
	private static final String EMPTY_HEADERS_PARAMETER = "empty-values"
	private static final String UNKNOWN_HEADERS_PARAMETER = "unknown-values"
	
	def "Scenario: parse key-value sets"() {
		given: "The headers map"
		Map<String, Collection<String>> headerValues = new HashMap();
		headerValues.put(EMPTY_HEADERS_PARAMETER, [null, "   "])
		headerValues.put(STRING_HEADERS_PARAMETER, ["1", "2 ", "3", "abcd"])
		headerValues.put(BYTE_HEADERS_PARAMETER, ["1", "2", "3", "abcd"])
		headerValues.put(SHORT_HEADERS_PARAMETER, ["1", "2", "3", "abcd"])
		headerValues.put(INTEGER_HEADERS_PARAMETER, ["1", "2", "3", "abcd "])
		headerValues.put(LONG_HEADERS_PARAMETER, ["1 ", "2", " 3", "abcd"])
		headerValues.put(FLOAT_HEADERS_PARAMETER, ["1", "2.0", "3", "abcd"])
		headerValues.put(DOUBLE_HEADERS_PARAMETER, ["1", "2", "3.0", "abcd"])
		
		expect: "The key-value parameters should be successfully initialized"
		KeyValueParameters httpHeaders = new KeyValueParameters(headerValues)
	
		and: "The key-value parameters should contain empty parameter set"
		httpHeaders.isContain(UNKNOWN_HEADERS_PARAMETER) == false
		httpHeaders.getValues(UNKNOWN_HEADERS_PARAMETER) == []
		httpHeaders.getStringValue(UNKNOWN_HEADERS_PARAMETER) == Optional.empty()
		
		and: "The key-value parameters should contain empty parameter set"
		httpHeaders.isContain(EMPTY_HEADERS_PARAMETER) == true
		httpHeaders.getValues(EMPTY_HEADERS_PARAMETER) == []
		httpHeaders.getStringValue(EMPTY_HEADERS_PARAMETER) == Optional.empty()
			
		and: "The key-value parameters should contain string parameter set"
		httpHeaders.isContain(STRING_HEADERS_PARAMETER) == true
		httpHeaders.getValues(STRING_HEADERS_PARAMETER) == ["1", "2", "3", "abcd"]
		httpHeaders.getStringValue(STRING_HEADERS_PARAMETER) == Optional.of("1")
		
		and: "The key-value parameters should contain byte parameter set"
		httpHeaders.isContain(BYTE_HEADERS_PARAMETER) == true
		httpHeaders.getByteValues(BYTE_HEADERS_PARAMETER) == [1, 2, 3]
		httpHeaders.getByteValue(BYTE_HEADERS_PARAMETER) == Optional.of((byte) 1)
		
		and: "The key-value parameters should contain short parameter set"
		httpHeaders.isContain(SHORT_HEADERS_PARAMETER) == true
		httpHeaders.getShortValues(SHORT_HEADERS_PARAMETER) == [1, 2, 3]
		httpHeaders.getShortValue(SHORT_HEADERS_PARAMETER) == Optional.of((short) 1)
		
		and: "The key-value parameters should contain int parameter set"
		httpHeaders.isContain(INTEGER_HEADERS_PARAMETER) == true
		httpHeaders.getIntValues(INTEGER_HEADERS_PARAMETER) == [1, 2, 3]
		httpHeaders.getIntValue(INTEGER_HEADERS_PARAMETER) == Optional.of(1)
		
		and: "The key-value parameters should contain long parameter set"
		httpHeaders.isContain(LONG_HEADERS_PARAMETER) == true
		httpHeaders.getLongValues(LONG_HEADERS_PARAMETER) == [1, 2, 3]
		httpHeaders.getLongValue(LONG_HEADERS_PARAMETER) == Optional.of((long) 1)
		
		and: "The key-value parameters should contain float parameter set"
		httpHeaders.isContain(FLOAT_HEADERS_PARAMETER) == true
		httpHeaders.getFloatValues(FLOAT_HEADERS_PARAMETER) == [1.0, 2.0, 3.0]
		httpHeaders.getFloatValue(FLOAT_HEADERS_PARAMETER) == Optional.of((float) 1.0)
		
		and: "The http parameter set should contain double parameter set"
		httpHeaders.isContain(DOUBLE_HEADERS_PARAMETER) == true
		httpHeaders.getDoubleValues(DOUBLE_HEADERS_PARAMETER) == [1.0, 2.0, 3.0]
		httpHeaders.getDoubleValue(DOUBLE_HEADERS_PARAMETER) == Optional.of((double) 1.0)
	}
}
