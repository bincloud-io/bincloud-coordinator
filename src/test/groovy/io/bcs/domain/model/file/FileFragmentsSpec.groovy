package io.bcs.domain.model.file

import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import io.bcs.domain.model.Constants
import spock.lang.Specification
import spock.lang.Unroll

class FileFragmentsSpec extends Specification {
	@Unroll
	def "Scenario: create file fragments"() {
		given: "The download file range: ${start} -> ${end}"
		Range inputRange = createStubRange(start, end)
		
		and: "The file fragments created for this range and file size ${totalSize}"
		FileFragments fragments = new FileFragments([inputRange], totalSize)
		
		expect: "The file fragments should contain 1 fragment" 
		fragments.getParts().size() == 1
		
		and: "The fragment range offset should be ${downloadOffset}"
		ContentFragment fragment = fragments.getParts()[0]
		fragment.getOffset() == downloadOffset
		
		and: "The fragment range size should be ${downloadSize}"
		fragment.getLength() == downloadSize
		
		where:
		start       | end       | totalSize      | downloadOffset    | downloadSize
		null        | null      | 1000L          | 0L                | 1000L
		100L        | null      | 1000L          | 100L              | 900L
		null        | 100L      | 1000L          | 900L              | 100L
		100L        | 199L      | 1000L          | 100L              | 100L
		0L          | 1999L     | 1000L          | 0L                | 1000L
		200L        | 200L      | 1000L          | 200L              | 1L
	}

	def "Scenario: create file fragments with wrong range state (end >= start)"() {
		given: "The wrong download file range: ${start} >= ${end}"
		Range inputRange = createStubRange(start, end)
		
		when: "The download range is created with this range"
		new FileFragments([inputRange], totalSize)
		
		then: "The unsatisfiable file range exception should be thrown"
		UnsatisfiableRangeFormatException error = thrown(UnsatisfiableRangeFormatException)
        error.getContextId() == Constants.CONTEXT
        error.getErrorSeverity() == ErrorSeverity.BUSINESS
        error.getErrorCode() == Constants.UNSATISFIABLE_RANGES_FORMAT_ERROR
        		
		where:
		start | end  | totalSize 
		200L  | 100L | 1000L
		200L  | 199L | 1000L
		-200L | null | 1000L
		200L  | 300L | 10L
	}
	
	def "Scenario: create file fragments for empty ranges collection"() {
		expect: "The empty fragments collection should be returned"
		FileFragments fragments = new FileFragments([], 1000L)
		fragments.getParts().size() == 0
	}
	
	private Range createStubRange(Long start, Long end) {
		Range inputRange = Stub(Range)
		inputRange.getStart() >> Optional.ofNullable(start)
		inputRange.getEnd() >> Optional.ofNullable(end)
		return inputRange
	}
}
