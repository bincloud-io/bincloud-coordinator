package io.bincloud.common.components.time

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bincloud.common.time.DateTime
import io.bincloud.common.time.DateTime.State
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To have the same mechanism of date and time representation and working with it
	in the whole system, as a developer I am needed in the value object which will
	be used instead of java.time.*, but use this "under the skin".  It will allow 
	us not only define a single mechanism of working with date and time, but determine
	internal state to make possible for storing this one in a database.
""")
class DateTimeSpec extends Specification {
	static final Instant BASE_POINT = Instant.now()
	
	def "Scenario: create date time by default"() {
		given: "The date time created with the default constructor"
		DateTime dateTime = new DateTime();
		
		expect: "The date time unit should contain current date and time"
		dateTime.toInstant() > Instant.now().minusMillis(1000)
		dateTime.toInstant() < Instant.now().plusMillis(1000)
		
		and: "The date time unit should be truncated to milliseconds"
		dateTime.internalState().getFraction() == 0
	}
	
	def "Scenario: create date time from the instant value"() {
		given: "The instant value"
		Instant instantValue = Instant.now()
		
		and: "The date time value obtained from this"
		DateTime dateTime = new DateTime(instantValue);
		
		expect: "The date time value will be transformed to the instant with the same value"
		dateTime.toInstant() == instantValue
	}
	
	def "Scenario: reproduce date time from internal state"() {
		given: "The existing date time internal state"
		Date unit = new Date()
		Long fraction = 1000L
		State dateTimeState = Stub(State)
		dateTimeState.getUnit() >> unit
		dateTimeState.getFraction() >> fraction
		
		and: "The date time created from existing state"
		DateTime dateTime = new DateTime(dateTimeState)
		
		expect: "The date time instant value should be the same as created from given unit and fraction"
		dateTime.toInstant() == Instant.ofEpochMilli(unit.getTime()).plusNanos(fraction)
	}
	
	def "Scenario: get date time for current instant"() {
		given: "The obtained current instant value using java.time.Instant"
		Instant instant = Instant.now()
		
		when: "The current date time moment has been obtained from DateTime"
		DateTime dateTime = DateTime.now()
		
		then: "The instant value obtained from DateTime object should be between instant - 10ms and instant + 10ms"
		dateTime.toInstant() > instant.minusMillis(10)
		dateTime.toInstant() < instant.plusMillis(10)
	}
	
	def "Scenario: get date time internal state snapshot"() {
		given: "The existing date time"
		DateTime sourceDateTime = new DateTime()
		
		when: "The date time internal state has been obtained"
		State internalState = sourceDateTime.internalState()
		
		and: "The new date time object has been reproduced from this one"
		DateTime reproducedDateTime = new DateTime(internalState)
		
		then: "These ones should be equal"
		sourceDateTime == reproducedDateTime
	}
	
	def "Scenario: increase date time to the future"() {
		given: "The instant value"
		Instant instantValue = Instant.now()
		
		and: "The date time value created by instant value"
		DateTime dateTimeValue = new DateTime(instantValue)
		
		when: "The amount has been increased for the instant value"
		instantValue = instantValue.plus(increaseAmount, increaseUnit)
		
		and: "The amount has been increased for date time"
		dateTimeValue = dateTimeValue.plus(increaseAmount, increaseUnit)
		
		then: "The instant value obtained from date time value should be equal to source instant value"
		dateTimeValue.toInstant() == instantValue
		
		where:
		increaseAmount   | increaseUnit
		10L              | ChronoUnit.SECONDS
		1000L            | ChronoUnit.NANOS
		1L               | ChronoUnit.MICROS
		100L             | ChronoUnit.DAYS
	}
	
	def "Scenario: decrease date time to the past"() {
		given: "The instant value"
		Instant instantValue = Instant.now()
		
		and: "The date time value created by instant value"
		DateTime dateTimeValue = new DateTime(instantValue)
		
		when: "The amount has been decreased for the instant value"
		instantValue = instantValue.minus(decreaseAmount, decreaseUnit)
		
		and: "The amount has been decreased for date time"
		dateTimeValue = dateTimeValue.minus(decreaseAmount, decreaseUnit)
		
		then: "The instant value obtained from date time value should be equal to source instant value"
		dateTimeValue.toInstant() == instantValue
		
		where:
		decreaseAmount   | decreaseUnit
		10L              | ChronoUnit.SECONDS
		1000L            | ChronoUnit.NANOS
		1L               | ChronoUnit.MICROS
		100L             | ChronoUnit.DAYS
	}
	
	def "Scenario: stringify date time"() {
		given: "The instant value"
		Instant instantValue = Instant.now()
		
		and: "The date time value created by instant value"
		DateTime dateTimeValue = new DateTime(instantValue)
		
		expect: "The stringify date time value should be the same as stringified instant value"
		dateTimeValue.toString() == instantValue.toString()
	}
	
	def "Scenario: compare with null"() {
		expect: "Date time value always will be greater than null"
		DateTime.now().compareTo(null) == 1
	}
	
	
	def "Scenario: compare date time values"() {
		given: "The date time values created from instants"
		DateTime leftDateTime = new DateTime(leftInstant)
		DateTime rightDateTime = new DateTime(rightInstant)
		
		expect: "The date time comparison result should be the same as instant comarison result"
		int instantsComparisonResult = leftInstant.compareTo(rightInstant)
		int dateTimesComparisonResult = leftDateTime.compareTo(rightDateTime)
		dateTimesComparisonResult == instantsComparisonResult
		
		where:
		
		leftInstant                   | rightInstant
		BASE_POINT.plusSeconds(100)   | BASE_POINT.minusSeconds(10)
		BASE_POINT.minusSeconds(100)  | BASE_POINT.minusSeconds(10)
		BASE_POINT.plusSeconds(100)   | BASE_POINT.plusSeconds(100)
	}
}
