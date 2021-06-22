package io.bincloud.resources.application.download

import groovy.cli.Option
import io.bincloud.resources.application.download.operations.DownloadRange
import io.bincloud.resources.domain.model.contracts.FileDownloader.Range
import spock.lang.Specification
import spock.lang.Unroll

class DownloadRangeSpec extends Specification {
	@Unroll
	def "Scenario: create download file range"() {
		given: "The download file range: ${start} -> ${end}"
		Range inputRange = Stub(Range)
		inputRange.getStart() >> Optional.ofNullable(start)
		inputRange.getEnd() >> Optional.ofNullable(end)
		
		and: "The download range created for this range and file size ${totalSize}"
		DownloadRange downloadRange = new DownloadRange(inputRange, totalSize)
		
		expect: "The download file range offset should be ${downloadOffset}"
		downloadRange.getStartPosition() == downloadOffset
		
		and: "The download file range size should be ${downloadSize}"
		downloadRange.getSize() == downloadSize
		
		where:
		start       | end       | totalSize      | downloadOffset    | downloadSize
		null        | null      | 1000L          | 0L                | 1000L
		100L        | null      | 1000L          | 100L              | 900L
		null        | 100L      | 1000L          | 900L              | 100L
		100L        | 200L      | 1000L          | 100L              | 100L
	}
	
}
