package io.bce.validation

import static io.bce.validation.Rules.*
import java.util.regex.Pattern

import io.bce.Range.ThresholdsAmountsException
import io.bce.text.TextTemplate
import io.bce.text.TextTemplates
import io.bce.validation.RuleExecutor.RuleExecutionReport
import io.bce.validation.Rules.RulePredicate;
import io.bce.validation.Rules.SizeMustNotBeNegativeValue
import io.bce.text.TextTemplates.DefaultTextTemplate
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class RulesSpec extends Specification {
	private static final Object SIMPLE_OBJECT = new Object()
	private static final DefaultTextTemplate FAIL_MESSAGE_TEMPLATE = TextTemplates.createBy("FAIL")
	private static final RulePredicate PASSED_PREDICATE = createStaticPredicate(true)
	private static final RulePredicate FAILED_PREDICATE = createStaticPredicate(false)
	private static final Pattern PATTERN = Pattern.compile("([A-Z])*")
	
	@Unroll
	def "Scenario: passed rules checks"() {
		expect:
		ruleExecutor.execute().ruleIsPassed()

		where:
		ruleExecutor                                                                                          | _
		new RuleExecutor(new Object(), {Rules.notNull(FAIL_MESSAGE_TEMPLATE)})                                | _
		new RuleExecutor(null, {Rules.isNull(FAIL_MESSAGE_TEMPLATE)})                                         | _
		new RuleExecutor(null, {Rules.match(Object.class, FAIL_MESSAGE_TEMPLATE, FAILED_PREDICATE)})          | _
		new RuleExecutor(10, {Rules.equalTo(10, FAIL_MESSAGE_TEMPLATE)})                                      | _
		new RuleExecutor(10, {Rules.notEqualTo(100, FAIL_MESSAGE_TEMPLATE)})                                  | _
		new RuleExecutor(true, {Rules.assertTrue(FAIL_MESSAGE_TEMPLATE)})                                     | _
		new RuleExecutor(false, {Rules.assertFalse(FAIL_MESSAGE_TEMPLATE)})                                   | _
		new RuleExecutor(100L, {Rules.greaterThan(Long, 10L, FAIL_MESSAGE_TEMPLATE)})                         | _
		new RuleExecutor(100L, {Rules.greaterThanOrEqual(Long, 10L, FAIL_MESSAGE_TEMPLATE)})                  | _
		new RuleExecutor(10L, {Rules.greaterThanOrEqual(Long, 10L, FAIL_MESSAGE_TEMPLATE)})                   | _
		new RuleExecutor(100L, {Rules.lessThan(Long, 1000L, FAIL_MESSAGE_TEMPLATE)})                          | _
		new RuleExecutor(100L, {Rules.lessThanOrEqual(Long, 1000L, FAIL_MESSAGE_TEMPLATE)})                   | _
		new RuleExecutor(1000L, {Rules.lessThanOrEqual(Long, 1000L, FAIL_MESSAGE_TEMPLATE)})                  | _
		new RuleExecutor(100L, {Rules.between(Long, 100L, 200L, FAIL_MESSAGE_TEMPLATE)})                      | _
		new RuleExecutor(150L, {Rules.between(Long, 100L, 200L, FAIL_MESSAGE_TEMPLATE)})                      | _
		new RuleExecutor(200L, {Rules.between(Long, 100L, 200L, FAIL_MESSAGE_TEMPLATE)})                      | _
		new RuleExecutor(99L, {Rules.outside(Long, 100L, 200L, FAIL_MESSAGE_TEMPLATE)})                       | _
		new RuleExecutor(201L, {Rules.outside(Long, 100L, 200L, FAIL_MESSAGE_TEMPLATE)})                      | _
		new RuleExecutor("HELLO", {Rules.hasLength(String, 5, FAIL_MESSAGE_TEMPLATE)})                        | _
		new RuleExecutor("HELLO", {Rules.doesNotHaveLength(String, 15, FAIL_MESSAGE_TEMPLATE)})               | _
		new RuleExecutor("", {Rules.empty(String, FAIL_MESSAGE_TEMPLATE)})                                    | _
		new RuleExecutor("HELLO", {Rules.notEmpty(String, FAIL_MESSAGE_TEMPLATE)})                            | _
		new RuleExecutor("HELLO", {Rules.minLength(String, 5, FAIL_MESSAGE_TEMPLATE)})                        | _
		new RuleExecutor("HELLO!", {Rules.minLength(String, 5, FAIL_MESSAGE_TEMPLATE)})                       | _
		new RuleExecutor("HELLO!", {Rules.maxLength(String, 6, FAIL_MESSAGE_TEMPLATE)})                       | _
		new RuleExecutor("HELLO", {Rules.maxLength(String, 6, FAIL_MESSAGE_TEMPLATE)})                        | _
		new RuleExecutor("HELLO", {Rules.limitedLength(String, 5, 6, FAIL_MESSAGE_TEMPLATE)})                 | _
		new RuleExecutor("HELLO", {Rules.limitedLength(String, 4, 5, FAIL_MESSAGE_TEMPLATE)})                 | _
		new RuleExecutor("HELLO", {Rules.limitedLength(String, 2, 10, FAIL_MESSAGE_TEMPLATE)})                | _
		new RuleExecutor("HELLO", {Rules.pattern(String, PATTERN, FAIL_MESSAGE_TEMPLATE)})                    | _
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.collectionHasSize(List, 5, FAIL_MESSAGE_TEMPLATE)})          | _
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.collectionDoesNotHaveSize(List, 6, FAIL_MESSAGE_TEMPLATE)})  | _
		new RuleExecutor([], {Rules.emptyCollection(List, FAIL_MESSAGE_TEMPLATE)})                            | _
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.notEmptyCollection(List, FAIL_MESSAGE_TEMPLATE)})            | _
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.minCollectionSize(List, 5, FAIL_MESSAGE_TEMPLATE)})          | _
		new RuleExecutor([1, 2, 3, 4, 5, 6], {Rules.minCollectionSize(List, 5, FAIL_MESSAGE_TEMPLATE)})       | _
		new RuleExecutor([1, 2, 3, 4, 5, 6], {Rules.maxCollectionSize(List, 6, FAIL_MESSAGE_TEMPLATE)})       | _
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.maxCollectionSize(List, 6, FAIL_MESSAGE_TEMPLATE)})          | _
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.limitedCollectionSize(List, 5, 6, FAIL_MESSAGE_TEMPLATE)})   | _
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.limitedCollectionSize(List, 4, 5, FAIL_MESSAGE_TEMPLATE)})   | _
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.limitedCollectionSize(List, 2, 10, FAIL_MESSAGE_TEMPLATE)})  | _
		new RuleExecutor(SIMPLE_OBJECT, {Rules.match(Object, FAIL_MESSAGE_TEMPLATE, PASSED_PREDICATE)})       | _
		
	}

	
	
	@Unroll
	def "Scenario: failed rules checks"() {
		expect:
		RuleExecutionReport report = ruleExecutor.execute()
		report.ruleIsFailed() == true
		report.contains(errorTemplates)
		
		where:
		ruleExecutor                                                                                              | errorTemplates
		new RuleExecutor(SIMPLE_OBJECT, {Rules.isNull(FAIL_MESSAGE_TEMPLATE)})                                    | [FAIL_MESSAGE_TEMPLATE.withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, Optional.of(SIMPLE_OBJECT))]
		new RuleExecutor(null, {Rules.notNull(FAIL_MESSAGE_TEMPLATE)})                                            | [FAIL_MESSAGE_TEMPLATE.withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, Optional.empty())]
		new RuleExecutor(11, {Rules.equalTo(10, FAIL_MESSAGE_TEMPLATE)})                                          | [FAIL_MESSAGE_TEMPLATE.withParameter(EXPECTED_VALUE_PARAMETER, 10).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, 11)]
		new RuleExecutor(10, {Rules.notEqualTo(10, FAIL_MESSAGE_TEMPLATE)})                                       | [FAIL_MESSAGE_TEMPLATE.withParameter(EXPECTED_VALUE_PARAMETER, 10).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, 10)]
		new RuleExecutor(false, {Rules.assertTrue(FAIL_MESSAGE_TEMPLATE)})                                        | [FAIL_MESSAGE_TEMPLATE.withParameter(EXPECTED_VALUE_PARAMETER, true).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, false)]
		new RuleExecutor(true, {Rules.assertFalse(FAIL_MESSAGE_TEMPLATE)})                                        | [FAIL_MESSAGE_TEMPLATE.withParameter(EXPECTED_VALUE_PARAMETER, false).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, true)]
		new RuleExecutor(9L, {Rules.greaterThan(Long, 10L, FAIL_MESSAGE_TEMPLATE)})                               | [FAIL_MESSAGE_TEMPLATE.withParameter(MIN_PARAMETER_VALUE, 10L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, 9L)]
		new RuleExecutor(9L, {Rules.greaterThanOrEqual(Long, 10L, FAIL_MESSAGE_TEMPLATE)})                        | [FAIL_MESSAGE_TEMPLATE.withParameter(MIN_PARAMETER_VALUE, 10L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, 9L)]
		new RuleExecutor(101L, {Rules.lessThan(Long, 100L, FAIL_MESSAGE_TEMPLATE)})                               | [FAIL_MESSAGE_TEMPLATE.withParameter(MAX_PARAMETER_VALUE, 100L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, 101L)]
		new RuleExecutor(101L, {Rules.lessThanOrEqual(Long, 100L, FAIL_MESSAGE_TEMPLATE)})                        | [FAIL_MESSAGE_TEMPLATE.withParameter(MAX_PARAMETER_VALUE, 100L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, 101L)]
		new RuleExecutor(3L, {Rules.between(Long, 5L, 7L, FAIL_MESSAGE_TEMPLATE)})                                | [FAIL_MESSAGE_TEMPLATE.withParameter(MIN_PARAMETER_VALUE, 5L).withParameter(MAX_PARAMETER_VALUE, 7L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, 3L)]
		new RuleExecutor(10L, {Rules.between(Long, 5L, 7L, FAIL_MESSAGE_TEMPLATE)})                               | [FAIL_MESSAGE_TEMPLATE.withParameter(MIN_PARAMETER_VALUE, 5L).withParameter(MAX_PARAMETER_VALUE, 7L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, 10L)]
		new RuleExecutor(6L, {Rules.outside(Long, 5L, 7L, FAIL_MESSAGE_TEMPLATE)})                                | [FAIL_MESSAGE_TEMPLATE.withParameter(MIN_PARAMETER_VALUE, 5L).withParameter(MAX_PARAMETER_VALUE, 7L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, 6L)]
		new RuleExecutor("HELL", {Rules.hasLength(String, 5L, FAIL_MESSAGE_TEMPLATE)})                            | [FAIL_MESSAGE_TEMPLATE.withParameter(EXPECTED_LENGTH_PARAMETER_VALUE, 5L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, "HELL")]
		new RuleExecutor("HELLO", {Rules.doesNotHaveLength(String, 5L, FAIL_MESSAGE_TEMPLATE)})                   | [FAIL_MESSAGE_TEMPLATE.withParameter(EXPECTED_LENGTH_PARAMETER_VALUE, 5L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, "HELLO")]
		new RuleExecutor("HELLO", {Rules.empty(String, FAIL_MESSAGE_TEMPLATE)})                                   | [FAIL_MESSAGE_TEMPLATE.withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, "HELLO")]
		new RuleExecutor("", {Rules.notEmpty(String, FAIL_MESSAGE_TEMPLATE)})                                     | [FAIL_MESSAGE_TEMPLATE.withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, "")]
		new RuleExecutor("HELL", {Rules.minLength(String, 5L, FAIL_MESSAGE_TEMPLATE)})                            | [FAIL_MESSAGE_TEMPLATE.withParameter(MIN_LENGTH_PARAMETER_VALUE, 5L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, "HELL")]
		new RuleExecutor("HELLO!", {Rules.maxLength(String, 5L, FAIL_MESSAGE_TEMPLATE)})                          | [FAIL_MESSAGE_TEMPLATE.withParameter(MAX_LENGTH_PARAMETER_VALUE, 5L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, "HELLO!")]
		new RuleExecutor("HELLO!!", {Rules.limitedLength(String, 5L, 6L, FAIL_MESSAGE_TEMPLATE)})                 | [FAIL_MESSAGE_TEMPLATE.withParameter(MIN_LENGTH_PARAMETER_VALUE, 5L).withParameter(MAX_LENGTH_PARAMETER_VALUE, 6L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, "HELLO!!")]
		new RuleExecutor("12345", {Rules.pattern(String, PATTERN, FAIL_MESSAGE_TEMPLATE)})                        | [FAIL_MESSAGE_TEMPLATE.withParameter(REGEXP_PARAMETER_VALUE, PATTERN).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, "12345")]
		new RuleExecutor([1, 2, 3, 4, 5, 6], {Rules.collectionHasSize(List, 5L, FAIL_MESSAGE_TEMPLATE)})          | [FAIL_MESSAGE_TEMPLATE.withParameter(EXPECTED_SIZE_PARAMETER_VALUE, 5L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, [1, 2, 3, 4, 5, 6])]
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.collectionDoesNotHaveSize(List, 5L, FAIL_MESSAGE_TEMPLATE)})     | [FAIL_MESSAGE_TEMPLATE.withParameter(EXPECTED_SIZE_PARAMETER_VALUE, 5L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, [1, 2, 3, 4, 5])]
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.emptyCollection(List, FAIL_MESSAGE_TEMPLATE)})                   | [FAIL_MESSAGE_TEMPLATE.withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, [1, 2, 3, 4, 5])]
		new RuleExecutor([], {Rules.notEmptyCollection(List, FAIL_MESSAGE_TEMPLATE)})                             | [FAIL_MESSAGE_TEMPLATE.withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, [])]
		new RuleExecutor([1, 2, 3, 4], {Rules.minCollectionSize(List, 5L, FAIL_MESSAGE_TEMPLATE)})                | [FAIL_MESSAGE_TEMPLATE.withParameter(MIN_SIZE_PARAMETER_VALUE, 5L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, [1, 2, 3, 4])]
		new RuleExecutor([1, 2, 3, 4, 5, 6], {Rules.maxCollectionSize(List, 5L, FAIL_MESSAGE_TEMPLATE)})          | [FAIL_MESSAGE_TEMPLATE.withParameter(MAX_SIZE_PARAMETER_VALUE, 5L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, [1, 2, 3, 4, 5, 6])]
		new RuleExecutor([1, 2, 3, 4, 5, 6], {Rules.limitedCollectionSize(List, 4L, 5L, FAIL_MESSAGE_TEMPLATE)})  | [FAIL_MESSAGE_TEMPLATE.withParameter(MIN_SIZE_PARAMETER_VALUE, 4L).withParameter(MAX_SIZE_PARAMETER_VALUE, 5L).withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, [1, 2, 3, 4, 5, 6])]
		new RuleExecutor(SIMPLE_OBJECT, {Rules.match(Object, FAIL_MESSAGE_TEMPLATE, FAILED_PREDICATE)})           | [FAIL_MESSAGE_TEMPLATE.withParameter(VALIDATED_ELEMENT_PARAMETER_NAME, SIMPLE_OBJECT)]
		
	}

	@Unroll
	def "Scenario: exceptional rules checks"() {
		expect:
		RuleExecutionReport report = ruleExecutor.execute()
		report.completedWithError()
		report.completedWith(thrownError)

				
		where:
		ruleExecutor                                                                                           | thrownError
		new RuleExecutor("HELLO", {Rules.hasLength(String, -5, FAIL_MESSAGE_TEMPLATE)})                        | SizeMustNotBeNegativeValue
		new RuleExecutor("HELLO", {Rules.doesNotHaveLength(String, -15, FAIL_MESSAGE_TEMPLATE)})               | SizeMustNotBeNegativeValue
		new RuleExecutor("HELLO", {Rules.minLength(String, -5, FAIL_MESSAGE_TEMPLATE)})                        | SizeMustNotBeNegativeValue	
		new RuleExecutor("HELLO", {Rules.maxLength(String, -6, FAIL_MESSAGE_TEMPLATE)})                        | SizeMustNotBeNegativeValue
		new RuleExecutor("HELLO", {Rules.limitedLength(String, -4, 5, FAIL_MESSAGE_TEMPLATE)})                 | SizeMustNotBeNegativeValue
		new RuleExecutor("HELLO", {Rules.limitedLength(String, 2, -10, FAIL_MESSAGE_TEMPLATE)})                | SizeMustNotBeNegativeValue
		new RuleExecutor("HELLO", {Rules.limitedLength(String, 10, 2, FAIL_MESSAGE_TEMPLATE)})                 | ThresholdsAmountsException
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.collectionHasSize(List, -5, FAIL_MESSAGE_TEMPLATE)})          | SizeMustNotBeNegativeValue
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.collectionDoesNotHaveSize(List, -6, FAIL_MESSAGE_TEMPLATE)})  | SizeMustNotBeNegativeValue
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.minCollectionSize(List, -5, FAIL_MESSAGE_TEMPLATE)})          | SizeMustNotBeNegativeValue
		new RuleExecutor([1, 2, 3, 4, 5, 6], {Rules.maxCollectionSize(List, -6, FAIL_MESSAGE_TEMPLATE)})       | SizeMustNotBeNegativeValue
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.limitedCollectionSize(List, -5, 6, FAIL_MESSAGE_TEMPLATE)})   | SizeMustNotBeNegativeValue
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.limitedCollectionSize(List, 5, -6, FAIL_MESSAGE_TEMPLATE)})   | SizeMustNotBeNegativeValue
		new RuleExecutor([1, 2, 3, 4, 5], {Rules.limitedCollectionSize(List, 6, 5, FAIL_MESSAGE_TEMPLATE)})    | ThresholdsAmountsException
	}

	private static final RulePredicate createStaticPredicate(boolean result) {
		return new RulePredicate() {
					@Override
					public boolean checkRuleFor(Object value) {
						return result;
					}
				}
	}
}
