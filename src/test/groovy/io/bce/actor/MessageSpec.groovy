package io.bce.actor

import groovy.transform.EqualsAndHashCode
import io.bce.actor.Message.MessageHandleFunction
import spock.lang.Specification

class MessageSpec extends Specification {
  private static final ActorAddress SENDER_ADDRESS = ActorAddress.ofUrn("urn:actor:SENDER")
  private static final ActorAddress RECEIVER_ADDRESS = ActorAddress.ofUrn("urn:actor:RECEIVER")
  private static final ActorAddress ANOTHER_ADDRESS = ActorAddress.ofUrn("urn:actor:ANOTHER")
  private static final CorrelationKey CORRELATION_KEY = CorrelationKey.wrap("123456")
  private static final CorrelationKey ANOTHER_CORRELATION_KEY = CorrelationKey.wrap("123456")

  def "Scenario: create message from unknown sender"() {
    expect: "The message should be created for unknown sender"
    Message<BodyObject> message = Message.createFor(RECEIVER_ADDRESS, new BodyObject())
    message.getCorrelationKey() == CorrelationKey.UNCORRELATED
    message.getSender() == ActorAddress.UNKNOWN_ADDRESS
    message.getDestination() == RECEIVER_ADDRESS
    message.getBody() == new BodyObject()
  }

  def "Scenario: create message from specified sender"() {
    expect: "The message should be created for specified sender"
    Message<BodyObject> message = Message.createFor(SENDER_ADDRESS, RECEIVER_ADDRESS, new BodyObject())
    message.getCorrelationKey() == CorrelationKey.UNCORRELATED
    message.getSender() == SENDER_ADDRESS
    message.getDestination() == RECEIVER_ADDRESS
    message.getBody() == new BodyObject()
  }

  def "Scenario: create message with specified correlation key from uncorrelated message"() {
    given: "The message without correlation key"
    Message<BodyObject> message = Message
        .createFor(SENDER_ADDRESS, RECEIVER_ADDRESS, new BodyObject())

    when: "The message is created with anothed correlation key is offered"
    message = message.correlateBy(ANOTHER_CORRELATION_KEY)

    then: "The message should be created with specified correlation key"
    message.getCorrelationKey() == ANOTHER_CORRELATION_KEY
    message.getSender() == SENDER_ADDRESS
    message.getDestination() == RECEIVER_ADDRESS
    message.getBody() == new BodyObject()
  }

  def "Scenario: create message with specified correlation key from correlated message"() {
    given: "The message with set correlation key"
    Message<BodyObject> message = Message
        .createFor(SENDER_ADDRESS, RECEIVER_ADDRESS, new BodyObject())
        .correlateBy(CORRELATION_KEY)

    when: "The message is created with anothed correlation key is offered"
    message = message.correlateBy(ANOTHER_CORRELATION_KEY)

    then: "The message shouldn't be created with changed correlation key"
    message.getCorrelationKey() == CORRELATION_KEY
    message.getSender() == SENDER_ADDRESS
    message.getDestination() == RECEIVER_ADDRESS
    message.getBody() == new BodyObject()
  }

  def "Scenario: replace message sender"() {
    given: "The source message object"
    Message<BodyObject> sourceMessage = Message
        .createFor(SENDER_ADDRESS, RECEIVER_ADDRESS, new BodyObject())
        .correlateBy(CORRELATION_KEY)

    when: "The sender address is replaced"
    Message<BodyObject> resultMessage = sourceMessage.withSender(ANOTHER_ADDRESS)

    then: "The new message object instance should be created with changed sender address"
    resultMessage.is(sourceMessage) == false
    resultMessage.getSender() == ANOTHER_ADDRESS
    resultMessage.getDestination() == sourceMessage.getDestination()
    resultMessage.getBody() == sourceMessage.getBody()

    and: "The correlation key shouldn't be changed"
    resultMessage.getCorrelationKey() == sourceMessage.getCorrelationKey()
  }

  def "Scenario: replace message destination"() {
    given: "The source message object"
    Message<BodyObject> sourceMessage = Message
        .createFor(SENDER_ADDRESS, RECEIVER_ADDRESS, new BodyObject())
        .correlateBy(CORRELATION_KEY)

    when: "The destination address is replaced"
    Message<BodyObject> resultMessage = sourceMessage.withDestination(ANOTHER_ADDRESS)

    then: "The new message object instance should be created with changed destination address"
    resultMessage.is(sourceMessage) == false
    resultMessage.getSender() == resultMessage.getSender()
    resultMessage.getDestination() == ANOTHER_ADDRESS
    resultMessage.getBody() == sourceMessage.getBody()

    and: "The correlation key shouldn't be changed"
    resultMessage.getCorrelationKey() == sourceMessage.getCorrelationKey()
  }

  def "Scenario: transform message body"() {
    given: "The source message object"
    Message<BodyObject> sourceMessage = Message.createFor(SENDER_ADDRESS, RECEIVER_ADDRESS, new BodyObject())

    when: "The message body is transformed"
    Message<String> resultMessage = sourceMessage.map({BodyObject body -> body.toString()})

    then: "The new message object instance should be created with changed body or/and body type"
    resultMessage.is(sourceMessage) == false
    resultMessage.getSender() == sourceMessage.getSender()
    resultMessage.getDestination() == sourceMessage.getDestination()
    resultMessage.getBody() instanceof String
    resultMessage.getBody() == "BODY_OBJECT"

    and: "The correlation key shouldn't be changed"
    resultMessage.getCorrelationKey() == sourceMessage.getCorrelationKey()
  }

  def "Scenario: create reply message to the current sender"() {
    given: "The source message object"
    Message<BodyObject> sourceMessage = Message.createFor(SENDER_ADDRESS, RECEIVER_ADDRESS, new BodyObject())

    when: "The reply message is created without destination specification"
    Message<String> replyMessage = sourceMessage.replyWith("OK")

    then: "The sender and destination should be swapped"
    replyMessage.getSender() == sourceMessage.getDestination()
    replyMessage.getDestination() == sourceMessage.getSender()

    and: "The correlation key shouldn't be changed"
    replyMessage.getCorrelationKey() == sourceMessage.getCorrelationKey()

    and: "The reply message body should be the same as it was passed to the method argument"
    replyMessage.getBody() == "OK"
  }

  def "Scenario: create reply message to the specified destination"() {
    given: "The source message object"
    Message<BodyObject> sourceMessage = Message.createFor(SENDER_ADDRESS, RECEIVER_ADDRESS, new BodyObject())

    when: "The reply message is created without destination specification"
    Message<String> replyMessage = sourceMessage.replyWith(ANOTHER_ADDRESS, "OK")

    then: "The reply sender should be swapped with source destination should be swapped"
    replyMessage.getSender() == sourceMessage.getDestination()

    and: "The correlation key shouldn't be changed"
    replyMessage.getCorrelationKey() == sourceMessage.getCorrelationKey()

    and: "The reply destination should be the same as it was passed to the method argument"
    replyMessage.getDestination() == ANOTHER_ADDRESS

    and: "The reply message body should be the same as it was passed to the method argument"
    replyMessage.getBody() == "OK"
  }

  def "Scenario: match message by type successfully without otherwise behavior"() {
    String bodyText
    given: "The message handle function"
    MessageHandleFunction<String> handleFunction = Mock(MessageHandleFunction)

    and: "The untyped message with correct type"
    Message<Object> message = Message.createFor(RECEIVER_ADDRESS, "Hello world!")

    when: "The message type is matched by class"
    message.whenIsMatchedTo(String, handleFunction)

    then: "The casted message body should be passed to the message handler function"
    1 * handleFunction.receive(_) >> {bodyText = it[0]}
    bodyText == "Hello world!"
  }

  def "Scenario: match message by type non successfully without otherwise behavior"() {
    String bodyText
    given: "The message handle function"
    MessageHandleFunction<String> handleFunction = Mock(MessageHandleFunction)

    and: "The untyped message with correct type"
    Message<Object> message = Message.createFor(RECEIVER_ADDRESS, new Object())

    when: "The message type is matched by class"
    message.whenIsMatchedTo(String, handleFunction)

    then: "The casted message body shouldn't be passed to the message handler function"
    0 * handleFunction.receive(_)
  }

  def "Scenario: match message by type successfully with otherwise behavior"() {
    String bodyText
    given: "The message handle function"
    MessageHandleFunction<String> handleFunction = Mock(MessageHandleFunction)

    and: "The otherwise handler function"
    MessageHandleFunction<String> otherwiseFunction = Mock(MessageHandleFunction)

    and: "The untyped message with correct type"
    Message<Object> message = Message.createFor(RECEIVER_ADDRESS, "Hello world!")

    when: "The message type is matched by class"
    message.whenIsMatchedTo(String, handleFunction, otherwiseFunction)

    then: "The casted message body should be passed to the message handler function"
    1 * handleFunction.receive(_) >> {bodyText = it[0]}
    bodyText == "Hello world!"

    and: "The message body shouldn't be passed to the otherwise handler function"
    0 * otherwiseFunction.receive(_)
  }

  def "Scenario: match message by type non successfully with otherwise behavior"() {
    String bodyText
    Object body = new Object()
    given: "The message handle function"
    MessageHandleFunction<String> handleFunction = Mock(MessageHandleFunction)

    and: "The otherwise handler function"
    MessageHandleFunction<String> otherwiseFunction = Mock(MessageHandleFunction)

    and: "The untyped message with correct type"
    Message<Object> message = Message.createFor(RECEIVER_ADDRESS, body)

    when: "The message type is matched by class"
    message.whenIsMatchedTo(String, handleFunction, otherwiseFunction)

    then: "The casted message body shouldn't be passed to the handler function"
    0 * handleFunction.receive(_)

    and: "The message body should be passed to the otherwise handler function"
    1 * otherwiseFunction.receive(body)
  }

  def "Scenario: match message by predicate successfully without otherwise behavior"() {
    given: "The message handle function"
    MessageHandleFunction<Long> handleFunction = Mock(MessageHandleFunction)

    and: "The message with correct even value"
    Message<Long> message = Message.createFor(RECEIVER_ADDRESS, 1000L)

    when: "The message is matched by even predicate"
    message.whenIsMatchedTo({Long val -> val % 2 == 0}, handleFunction)

    then: "The message should be passed to the message handler function"
    1 * handleFunction.receive(1000L)
  }

  def "Scenario: match message by predicate non successfully without otherwise behavior"() {
    given: "The message handle function"
    MessageHandleFunction<Long> handleFunction = Mock(MessageHandleFunction)

    and: "The message with non even value"
    Message<Long> message = Message.createFor(RECEIVER_ADDRESS, 1001L)

    when: "The message is unmatched by even predicate"
    message.whenIsMatchedTo({Long val -> val % 2 == 0}, handleFunction)

    then: "The message should be passed to the message handler function"
    0 * handleFunction.receive(_)
  }

  def "Scenario: match message by predicate successfully with otherwise behavior"() {
    given: "The message handle function"
    MessageHandleFunction<Long> handleFunction = Mock(MessageHandleFunction)

    and: "The otherwise handler function"
    MessageHandleFunction<String> otherwiseFunction = Mock(MessageHandleFunction)

    and: "The message with correct even value"
    Message<Long> message = Message.createFor(RECEIVER_ADDRESS, 1000L)

    when: "The message is matched by even predicate"
    message.whenIsMatchedTo({Long val -> val % 2 == 0}, handleFunction, otherwiseFunction)

    then: "The message should be passed to the handler function"
    1 * handleFunction.receive(1000L)

    and: "The otherwise fuction shouldn't be called"
    0 * otherwiseFunction.receive(_)
  }

  def "Scenario: match message by predicate non successfully with otherwise behavior"() {
    given: "The message handle function"
    MessageHandleFunction<Long> handleFunction = Mock(MessageHandleFunction)

    and: "The otherwise handler function"
    MessageHandleFunction<String> otherwiseFunction = Mock(MessageHandleFunction)

    and: "The message with odd value"
    Message<Long> message = Message.createFor(RECEIVER_ADDRESS, 1001L)

    when: "The message is unmatched by even predicate"
    message.whenIsMatchedTo({Long val -> val % 2 == 0}, handleFunction, otherwiseFunction)

    then: "The message shouldn't be passed to the message handler function"
    0 * handleFunction.receive(_)

    and: "The message should be passed to the otherwise handler function"
    1 * otherwiseFunction.receive(1001L)
  }


  @EqualsAndHashCode
  class BodyObject {
    @Override
    public String toString() {
      return "BODY_OBJECT";
    }
  }
}
