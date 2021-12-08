package io.bce.validation

import io.bce.validation.ValidationGroup.WrongValidationGroupFormatException
import spock.lang.Specification

class ValidationGroupSpec extends Specification {
  def "Scenario: create validation group from correct name"() {
    expect: "The validation group should be created correctly for well-formatted group name"
    ValidationGroup group = ValidationGroup.createFor("GROUP_NAME")

    and: "The validation group should be stringified to the original group name without changes"
    group.toString() == "GROUP_NAME"
  }

  def "Scenario: create validation group from wrong-formatted name"() {
    when: "The validation group is created from wrong-formatted name"
    ValidationGroup.createFor(groupName)

    then: "The wrong validation group format exception should be happened"
    thrown(WrongValidationGroupFormatException)

    where:
    groupName << [
      "",
      "Group Name",
      " GroupName",
      "GroupName "
    ]
  }

  def "Scenario: ungrouped validation group name"() {
    ValidationGroup ungroupedValue = ValidationGroup.UNGROUPED
    expect: "The value should be \$\$__UNGROUPED_MESSAGES__\$\$"
    ungroupedValue.toString() == "\$\$__UNGROUPED_MESSAGES__\$\$"
  }

  def "Scenario: derive non-reserved validation group for a subgroup "() {
    given: "The non-root validation group"
    ValidationGroup baseGroup = ValidationGroup.createFor("base")

    and: "The validation sub-group"
    ValidationGroup subGroup = ValidationGroup.createFor("derived")

    when: "The derived group is created for base group by the sub-group"
    ValidationGroup resultGroup = baseGroup.deriveWith(subGroup)

    then: "The result derived group name should follow to the pattern \"<base group>.<sub-group>\""
    resultGroup.toString() == "base.derived"
  }

  def "Scenario: derive ungrouped reserved validation group for a subgroup "() {
    given: "The ungrouped validation group"
    ValidationGroup rootGroup = ValidationGroup.UNGROUPED

    and: "The validation sub-group"
    ValidationGroup subGroup = ValidationGroup.createFor("derived")

    when: "The derived group is created for root group by the sub-group"
    ValidationGroup resultGroup = rootGroup.deriveWith(subGroup)

    then: "The result derived group name should be equivalent to sub-group"
    resultGroup.toString() == "derived"
  }

  def "Scenario: derive group for the ungrouped reserverved subgroup"() {
    given: "The non-reserved validation group"
    ValidationGroup rootGroup = ValidationGroup.createFor("base")

    and: "The validation sub-group"
    ValidationGroup subGroup = ValidationGroup.UNGROUPED

    when: "The derived group is created for root group by the sub-group"
    ValidationGroup resultGroup = rootGroup.deriveWith(subGroup)

    then: "The result derived group name should be equivalent to sub-group"
    resultGroup.toString() == "base"
  }
}
