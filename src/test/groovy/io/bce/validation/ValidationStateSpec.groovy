package io.bce.validation

import io.bce.validation.ValidationState.ErrorState
import spock.lang.Specification

class ValidationStateSpec extends Specification {
  private static final String VALIDATION_CONTEXT = "COMMON__VALIDATION";

  private static final String GROUP_NAME = "group"
  private static final ValidationGroup GROUP = ValidationGroup.createFor(GROUP_NAME)

  private static final String GROUP_1_NAME = "group.1"
  private static final ValidationGroup GROUP_1 = ValidationGroup.createFor(GROUP_1_NAME)

  private static final String GROUP_2_NAME = "group.2"
  private static final ValidationGroup GROUP_2 = ValidationGroup.createFor(GROUP_2_NAME)

  private static final String BASE_GROUP_NAME = "base"
  private static final ValidationGroup BASE_GROUP = ValidationGroup.createFor(BASE_GROUP_NAME)
  private static final String DERIVED_BASE_GROUP_NAME = "base.group"
  private static final ValidationGroup DERIVED_BASE_GROUP = ValidationGroup.createFor(DERIVED_BASE_GROUP_NAME)

  private static final ErrorMessage SIMPLE_MESSAGE = ErrorMessage.createFor("simple-message")
  private static final ErrorMessage GROUPED_MESSAGE = ErrorMessage.createFor("grouped_message")
  private static final ErrorMessage UNGROUPED_MESSAGE_1 = ErrorMessage.createFor("ungrouped_message_1")
  private static final ErrorMessage UNGROUPED_MESSAGE_2 = ErrorMessage.createFor("ungrouped_message_2")
  private static final ErrorMessage MESSAGE_1 = ErrorMessage.createFor("message_1")
  private static final ErrorMessage MESSAGE_2 = ErrorMessage.createFor("message_2")

  def "Scenario: create empty validation state"() {
    given: "The empty validation state"
    ValidationState validationState = new ValidationState()

    expect: "The error state is valid"
    ErrorState errorState = validationState.getErrorState()
    validationState.isValid() == true

    and: "The grouped errors in error state are empty"
    errorState.getGroupedErrors().isEmpty() == true

    and: "The ungrouped errors in error state are empty"
    errorState.getUngroupedErrors().isEmpty() == true
  }

  def "Scenario: create validation state with grouped errors"() {

    given: "The validation state with grouped errors"
    ValidationState validationState = new ValidationState()
        .withGrouped(GROUP, SIMPLE_MESSAGE);

    expect: "The validation state is invalid"
    ErrorState errorState = validationState.getErrorState()
    validationState.isValid() == false

    and: "The grouped errors in error state contain known error message"
    GroupedErrors.errorsOf(GROUP_NAME, errorState).containsAll([SIMPLE_MESSAGE])

    and: "The ungrouped errors in error state are empty"
    errorState.getUngroupedErrors().isEmpty() == true
  }

  def "Scenario: create validation state with ungrouped errors"() {
    given: "The validation state with ungrouped errors"
    ValidationState validationState = new ValidationState()
        .withUngrouped(SIMPLE_MESSAGE);

    expect: "The validation state is invalid"
    ErrorState errorState = validationState.getErrorState()
    validationState.isValid() == false

    and: "The grouped errors in error state are empty"
    errorState.getGroupedErrors().isEmpty() == true

    and: "The ungrouped errors in error state aren't empty"
    errorState.ungroupedErrors.isEmpty() == false

    and: "The ungrouped errors in error state contain known error message"
    errorState.ungroupedErrors.contains(SIMPLE_MESSAGE) == true
  }

  def "Scenario: create validation state with mixed types errors"() {
    given: "The validation state with grouped and ungrouped errors"
    ValidationState validationState = new ValidationState()
        .withGrouped(GROUP, GROUPED_MESSAGE)
        .withUngrouped(UNGROUPED_MESSAGE_1)
        .withUngrouped(UNGROUPED_MESSAGE_2);

    expect: "The validation state is invalid"
    ErrorState errorState = validationState.getErrorState()
    validationState.isValid() == false

    and: "The grouped errors in error state contain known error message"
    GroupedErrors.errorsOf(GROUP_NAME, errorState).containsAll([GROUPED_MESSAGE])

    and: "The ungrouped errors in error state contain known error message"
    errorState.getUngroupedErrors().containsAll([
      UNGROUPED_MESSAGE_1,
      UNGROUPED_MESSAGE_2
    ])
  }

  def "Scenario: merge two validation states"() {
    given: "The source validation state"
    ValidationState sourceState = new ValidationState()
        .withGrouped(GROUP_1, MESSAGE_1)
        .withGrouped(GROUP_1, MESSAGE_2)

    and: "The state to merge"
    ValidationState stateToMerge = new ValidationState()
        .withUngrouped(UNGROUPED_MESSAGE_1)
        .withGrouped(GROUP_1, MESSAGE_1)
        .withGrouped(GROUP_2, MESSAGE_1)

    when: "States are merged"
    ValidationState result = sourceState.merge(stateToMerge)

    then: "The result state should contain messages from source state and merged state without duplicates"
    ErrorState errorState = result.getErrorState()
    errorState.getUngroupedErrors().containsAll([UNGROUPED_MESSAGE_1])

    GroupedErrors.errorsOf(GROUP_1_NAME, errorState).containsAll([MESSAGE_1, MESSAGE_2])
    GroupedErrors.errorsOf(GROUP_2_NAME, errorState).containsAll([MESSAGE_1])
  }

  def "Scenario: create validation state as subgroup of specified group"() {
    given: "The source validation state"
    ValidationState sourceState = new ValidationState()
        .withUngrouped(UNGROUPED_MESSAGE_1)
        .withGrouped(GROUP, GROUPED_MESSAGE)

    when: "The validation state is derived from the specified group"
    ErrorState errorState =  sourceState.asSubgroup(BASE_GROUP).getErrorState()

    then: "All grouped messages groups should be derived from the base group"
    GroupedErrors.errorsOf(DERIVED_BASE_GROUP_NAME, errorState).containsAll([GROUPED_MESSAGE])


    and: "All ungrouped message should stay grouped with base group group name"
    GroupedErrors.errorsOf(BASE_GROUP_NAME, errorState).containsAll([UNGROUPED_MESSAGE_1])

    errorState.getUngroupedErrors().isEmpty() == true
  }

  def "Scenario: create validation state with message groups derived from specified group"() {
    given: "The source validation state"
    ValidationState sourceState = new ValidationState()
        .withUngrouped(UNGROUPED_MESSAGE_1)
        .withGrouped(GROUP, GROUPED_MESSAGE)

    when: "The validation state is derived from the specified group"
    ErrorState errorState =  sourceState.asDerivedFrom(BASE_GROUP).getErrorState()

    then: "All grouped messages groups should be derived from the base group"
    GroupedErrors.errorsOf(DERIVED_BASE_GROUP_NAME, errorState).containsAll([GROUPED_MESSAGE])

    and: "All ungrouped message should be the same as in the source state"
    errorState.getUngroupedErrors().containsAll([UNGROUPED_MESSAGE_1])
  }
}
