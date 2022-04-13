package io.bcs.fileserver.infrastructure.file
import io.bcs.fileserver.domain.errors.UnsatisfiableRangeFormatException
import io.bcs.fileserver.domain.model.file.content.download.Range
import io.bcs.fileserver.infrastructure.file.HttpRanges
import spock.lang.Specification

class HttpRangesSpec extends Specification {
  def "Scenario: successfully parse http ranges"() {
    given: "The http ranges value"
    HttpRanges ranges = new HttpRanges(Optional.of("bytes=-12,10-55,1000-"))

    expect: "The ranges should be correctly parsed"
    List<Range> parsedRanges = new ArrayList(ranges.getRanges())
    Range firstRange = parsedRanges[0]
    Range secondRange = parsedRanges[1]
    Range thirdRange = parsedRanges[2]
    firstRange.getStart().isPresent() == false
    firstRange.getEnd().get() == 12
    secondRange.getStart().get() == 10
    secondRange.getEnd().get() == 55
    thirdRange.getStart().get() == 1000
    thirdRange.getEnd().isPresent() == false
  }

  def "Scenario: parse empty ranges value"() {
    given: "The http ranges value"
    HttpRanges ranges = new HttpRanges(Optional.empty())

    expect: "The empty ranges collection should be received"
    ranges.getRanges().isEmpty() == true
  }

  def "Scenario: parse wrong format"() {
    when: "The wrong format is parsed"
    new HttpRanges(Optional.of("bytes=11-12-12")).getRanges()

    then: "The unsatisfied range format should be happened"
    thrown(UnsatisfiableRangeFormatException)
  }
}
