package io.bce.validation

import io.bce.validation.RuleExecutor.RuleExecutionReport
import io.bce.validation.ValidationContext.Rule
import spock.lang.Specification

class GlobalValidationsSpec extends Specification {
	private static final ErrorMessage GREATER_THAN_ERROR_MESSAGE = ErrorMessage.createFor("GREATER THAN FAILED")
	private static final String UNKNOWN_ALIAS = "UNKNOWN"
	private static final String GREATER_THAN_ALIAS = "GREATER_THAN"
	private static final Rule<Integer> GREATER_THAN_REGISTERED_RULE = Rules.greaterThan(Integer, 100, GREATER_THAN_ERROR_MESSAGE)
	
	def setup() {
		GlobalValidations.registerRule(GREATER_THAN_ALIAS, GREATER_THAN_REGISTERED_RULE)
	}
	
	def "Scenario: execute unknown rule"() {
		expect: "Rule should be ignored"
		RuleExecutor ruleExecutor = new RuleExecutor(new Object(), {GlobalValidations.getRule(UNKNOWN_ALIAS)})
		RuleExecutionReport report = ruleExecutor.execute()
		report.isAcceptable() == false
		report.ruleIsPassed() == true
		
	}
	
	def "Scenario: execute rule with not allowed type"() {
		expect: "Rule should be ignored"
		RuleExecutor ruleExecutor = new RuleExecutor(new Object(), {GlobalValidations.getRule(GREATER_THAN_ALIAS)})
		RuleExecutionReport report = ruleExecutor.execute()
		report.isAcceptable() == false
		report.ruleIsPassed() == true
	}
	
	def "Scenario: execute passed rule with allowed type"() {
		expect: "Rule should be failed if registered rule is failed"
		RuleExecutor ruleExecutor = new RuleExecutor(1000, {GlobalValidations.getRule(GREATER_THAN_ALIAS)})
		RuleExecutionReport report = ruleExecutor.execute()
		report.isAcceptable() == true
		report.ruleIsPassed() == true
		report.contains([])
	}
	
	def "Scenario: execute failed rule with allowed type"() {
		expect: "Rule should be failed if registered rule is failed"
		RuleExecutor ruleExecutor = new RuleExecutor(1, {GlobalValidations.getRule(GREATER_THAN_ALIAS)})
		RuleExecutionReport report = ruleExecutor.execute()
		report.isAcceptable() == true
		report.ruleIsPassed() == false
		report.contains([GREATER_THAN_ERROR_MESSAGE.withParameter(Rules.MIN_PARAMETER_VALUE, 100).withParameter(Rules.VALIDATED_ELEMENT_PARAMETER_NAME, 1)])
	}
	
	def "Scenario: execute inverted rule with allowed type"() {
		expect: "Rule should be failed if registered rule is failed"
		RuleExecutor ruleExecutor = new RuleExecutor(1, {GlobalValidations.getRule(GREATER_THAN_ALIAS).invert()})
		RuleExecutionReport report = ruleExecutor.execute()
		report.isAcceptable() == true
		report.ruleIsPassed() == true
		report.contains([])
	}
	
	def cleanup() {
		GlobalValidations.clear()
	}
}
