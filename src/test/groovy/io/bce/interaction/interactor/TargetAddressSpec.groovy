package io.bce.interaction.interactor

import io.bce.Urn.WrongUrnAddressFormatException
import io.bce.interaction.interactor.TargetAddress
import spock.lang.Specification

class TargetAddressSpec extends Specification {
  private static final String CORRECT_URN_ADDRESS_STRING = "urn:command:CREATE_SOMETHING";
  private static final String WRONG_URN_ADDRESS_STRING = "wrong-urn:";
  def "Scenario: create target address from correct urn address format"() {
    when: "The target address is created from well formatted urn address string value"
    TargetAddress targetAddress = TargetAddress.ofUrn(CORRECT_URN_ADDRESS_STRING)

    then: "The target address should be correctly created"
    targetAddress.toString() == CORRECT_URN_ADDRESS_STRING
  }

  def "Scenario: create target address from wrong urn address format"() {
    when: "The target address is created from wrong formatted urn address string value"
    TargetAddress.ofUrn(WRONG_URN_ADDRESS_STRING)

    then: "The wrong urn address format exception should be happened"
    thrown(WrongUrnAddressFormatException)
  }
}
