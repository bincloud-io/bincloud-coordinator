package io.bincloud.resources.application.download

import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.resources.domain.model.Constants
import io.bincloud.resources.domain.model.contracts.download.Range
import io.bincloud.resources.domain.model.errors.UnsatisfiableRangeFormatException
import spock.lang.Specification
import spock.lang.Unroll

class StoredFileFragmentSpec extends Specification {
	@Unroll
	def "Scenario: create download file range"() {
		given: "The download file range: ${start} -> ${end}"
		Range inputRange = Stub(Range)
		inputRange.getStart() >> Optional.ofNullable(start)
		inputRange.getEnd() >> Optional.ofNullable(end)
		
		and: "The download range created for this range and file size ${totalSize}"
		StoredFileFragment downloadRange = new StoredFileFragment(inputRange, totalSize)
		
		expect: "The download file range offset should be ${downloadOffset}"
		downloadRange.getStart() == downloadOffset
		
		and: "The download file range size should be ${downloadSize}"
		downloadRange.getSize() == downloadSize
		
		where:
		start       | end       | totalSize      | downloadOffset    | downloadSize
		null        | null      | 1000L          | 0L                | 1000L
		100L        | null      | 1000L          | 100L              | 900L
		null        | 100L      | 1000L          | 900L              | 100L
		100L        | 199L      | 1000L          | 100L              | 100L
		0L          | 1999L     | 1000L          | 0L                | 1000L
		200L        | 200L      | 1000L          | 200L              | 1L
	}

	def "Scenario: create download file range with wrong range state (end >= start)"() {
		given: "The wrong download file range: ${start} >= ${end}"
		Range inputRange = Stub(Range)
		inputRange.getStart() >> Optional.ofNullable(start)
		inputRange.getEnd() >> Optional.ofNullable(end)
		
		when: "The download range is created with this range"
		new StoredFileFragment(inputRange, totalSize)
		
		then: "The wrong file range exception should be thrown"
		UnsatisfiableRangeFormatException error = thrown()
		error.getContext() == Constants.CONTEXT
		error.getErrorCode() == UnsatisfiableRangeFormatException.ERROR_CODE
		error.getSeverity() == Severity.BUSINESS
		
		where:
		start | end  | totalSize 
		200L  | 100L | 1000L
		200L  | 199L | 1000L
		-200L | null | 1000L
		200L  | 300L | 10L
	}	
}
