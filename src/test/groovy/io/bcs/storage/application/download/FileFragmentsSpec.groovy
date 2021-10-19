package io.bcs.storage.application.download

import io.bcs.common.domain.model.error.ApplicationException.Severity
import io.bcs.storage.application.download.FileFragments
import io.bcs.storage.domain.model.contracts.download.Fragment
import io.bcs.storage.domain.model.contracts.download.Range
import io.bcs.storage.domain.model.errors.UnsatisfiableRangeFormatException
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
		Fragment fragment = fragments.getSinglePart()
		fragment.getStart() == downloadOffset
		
		and: "The fragment range size should be ${downloadSize}"
		fragment.getSize() == downloadSize
		
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
		
		then: "The wrong file range exception should be thrown"
		UnsatisfiableRangeFormatException error = thrown()
		error.getContext() == UnsatisfiableRangeFormatException.CONTEXT
		error.getErrorCode() == UnsatisfiableRangeFormatException.ERROR_CODE
		error.getSeverity() == Severity.BUSINESS
		
		where:
		start | end  | totalSize 
		200L  | 100L | 1000L
		200L  | 199L | 1000L
		-200L | null | 1000L
		200L  | 300L | 10L
	}
	
	def "Scenario: create file fragments for empty ranges collection"() {
		expect: "The whole file size fragment should be returned"
		FileFragments fragments = new FileFragments([], 1000L)
		fragments.getParts().size() == 1
		Fragment fragment = fragments.getSinglePart()
		fragment.getStart() == 0L
		fragment.getSize() == 1000L
	}
	
	def "Scenario: check that requested multiple fragments"() {
		expect: "Multiple fragments is requested only if ranges count > 1"
		new FileFragments(ranges, 1000L).isRequestedMultipleFragments() == isMultipleFragmentsRequested
		where:
		ranges                                          || isMultipleFragmentsRequested
		[]                                              || false
		[createStubRange(0, 1)]                         || false
		[createStubRange(0, 1), createStubRange(1, 1)]  || true
		          
	}
	
	private Range createStubRange(Long start, Long end) {
		Range inputRange = Stub(Range)
		inputRange.getStart() >> Optional.ofNullable(start)
		inputRange.getEnd() >> Optional.ofNullable(end)
		return inputRange
	}
}
