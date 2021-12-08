package io.bce.actor

import io.bce.actor.Actor.Context
import io.bce.actor.Actor.Factory
import io.bce.actor.Actor.FaultResolver
import io.bce.actor.Actor.LifecycleController
import io.bce.actor.ActorSystemSpec.PrinterLocalUsingActor.PrintLocal
import io.bce.actor.Actors.ActorDuplicationException
import io.bce.actor.Actors.SystemConfigurer
import io.bce.actor.EventLoop.Dispatcher
import spock.lang.Specification

class ActorSystemSpec extends Specification {
  private static final ActorAddress BOB_ACTOR_ADDRESS = ActorAddress.ofUrn("urn:actor:BOB")
  private static final ActorAddress ALICE_ACTOR_ADDRESS = ActorAddress.ofUrn("urn:actor:ALICE")
  private static final CorrelationKey GENERATED_KEY = CorrelationKey.wrap("CORRELATED_KEY")

  def "Scenario: tell message to the simple printer actor, registered in the actor system"() {
    given: "The actors system"
    FixedMessagesWaitingDispatcher dispatcher = FixedMessagesWaitingDispatcher.singleThreadDispatcher(3)
    ActorSystem actorSystem = createActorSystem(dispatcher)

    and: "The actor system is started"
    actorSystem.start()

    and: "The string buffer to which the actor will write input values"
    StringWriter printWriter = new StringWriter();

    and: "The registered printer actor, printing message \"The actor <actor urn> has sent the message: <input message>\""
    ActorAddress actorAddress = actorSystem.actorOf(PrinterActor.PRINT_ACTOR_NAME, PrinterActor.factory(printWriter))

    when: "The phrase \"Hello world!\" is told by unknown sender"
    CorrelationKey helloWorldKey = actorSystem.tell(Message.createFor(actorAddress, "Hello world!"))

    and: "The phrase \"Nice to meet you!\" is told by the actor with name \"BOB\""
    CorrelationKey niceToMeetYouKey = actorSystem.tell(Message.createFor(BOB_ACTOR_ADDRESS, actorAddress, "Nice to meet you!"))

    and: "The phrase \"Nice to meet you too!\" is told by the actor with address \"urn:actor:ALICE\""
    CorrelationKey niceToMeetYouTooKey = actorSystem.tell(Message.createFor(ALICE_ACTOR_ADDRESS, actorAddress, "Nice to meet you too!"))

    and: "The actor system is stopped after invocation"
    dispatcher.getWaiter().await()
    actorSystem.shutdown()

    then: "The first printed phrase should be: \"Printer is started!\""
    Collection<String> printPhrases = Arrays.asList(printWriter.getBuffer().toString().split("\r\n"));
    printPhrases.contains("Printer is started!")

    and: "The next printed phrase should be: \"The actor urn:actor:SYSTEM.DEAD_LETTER has sent the message: \"Hello world!\"\""
    printPhrases.contains("The actor urn:actor:SYSTEM.DEAD_LETTER has sent the message: \"Hello world!\"")

    and: "The next printed phrase should be: \"The actor urn:actor:SYSTEM.BOB has sent the message: \"Nice to meet you!\"\""
    printPhrases.contains("The actor urn:actor:BOB has sent the message: \"Nice to meet you!\"")

    and: "The next printed phrase should be: \"The actor urn:actor:SYSTEM.ALICE has sent the message: \"Nice to meet you too!\"\""
    printPhrases.contains("The actor urn:actor:ALICE has sent the message: \"Nice to meet you too!\"")

    and: "The last printed phrase should be: \"Printer is stopped!\""
    printPhrases.contains("Printer is stopped!")

    and: "The correlation keys should be assigned by system"
    helloWorldKey != CorrelationKey.UNCORRELATED
    niceToMeetYouKey != CorrelationKey.UNCORRELATED
    niceToMeetYouTooKey != CorrelationKey.UNCORRELATED
  }

  def "Scenario: handle errors with restart error handling strategy"() {
    given: "The actors system"
    FixedMessagesWaitingDispatcher dispatcher = FixedMessagesWaitingDispatcher.singleThreadDispatcher(11)
    ActorSystem actorSystem = createActorSystem(dispatcher)

    and: "The actor system is started"
    actorSystem.start()

    and: "The string buffer to which the actor will write input values"
    StringWriter printWriter = new StringWriter();

    and: "The registered printer actor, printing message \"The actor <actor urn> has sent the message: <input message>\""
    ActorAddress printerAddress = actorSystem.actorOf(PrinterActor.PRINT_ACTOR_NAME, PrinterActor.factory(printWriter))

    and: "The registered error producer actor with resume error handling strategy"
    ActorAddress resumeStrategyErrorProducer = actorSystem.actorOf(
        ErrorProducerActor.RESUME_STRATEGY_ERROR_HANDLER_ADDRESS,
        ErrorProducerActor.resumeErrorHandlingStrategyActor(printerAddress))

    and: "The registered error producer actor with restart error handling strategy"
    ActorAddress restartStrategyErrorProducer = actorSystem.actorOf(
        ErrorProducerActor.RESTART_STRATEGY_ERROR_HANDLER_ADDRESS,
        ErrorProducerActor.restartErrorHandlingStrategyActor(printerAddress))

    and: "The registered error producer actor with default error handling strategy"
    ActorAddress defaultStrategyErrorProducer = actorSystem.actorOf(
        ErrorProducerActor.DEFAULT_STRATEGY_ERROR_HANDLER_ADDRESS,
        ErrorProducerActor.defaultErrorHandlingStrategyActor(printerAddress))

    when: "The phrase \"Hello world!\" is told the error produced actor using resume handling strategy"
    actorSystem.tell(Message.createFor(resumeStrategyErrorProducer, "Hello world!"))

    and: "The phrase \"Nice to meet you!\" is told to the error produced actor using restart handling strategy"
    actorSystem.tell(Message.createFor(BOB_ACTOR_ADDRESS, restartStrategyErrorProducer, "Nice to meet you!"))

    and: "The phrase \"Nice to meet you too!\" is told by the actor using default handling strategy"
    actorSystem.tell(Message.createFor(ALICE_ACTOR_ADDRESS, defaultStrategyErrorProducer, "Nice to meet you too!"))

    and: "The actor system is stopped after invocation"
    dispatcher.getWaiter().await()
    actorSystem.shutdown()

    then: "The printed phrases should contain the phrase: \"Printer is started!\""
    Collection phrases = Arrays.asList(printWriter.getBuffer().toString().split("\r\n"))
    phrases.contains("Printer is started!") == true

    and: "The printed phrases should contain the phrase: \"The actor urn:actor:RESUME_ERROR_HANDLER has sent the message: \"Actor urn:actor:RESUME_ERROR_HANDLER started!\"\""
    phrases.contains("The actor urn:actor:RESUME_ERROR_HANDLER has sent the message: \"Actor urn:actor:RESUME_ERROR_HANDLER started!\"")

    and: "The printed phrases should contain the phrase: \"The actor urn:actor:RESTART_ERROR_HANDLER has sent the message: \"Actor urn:actor:RESTART_ERROR_HANDLER started!\"\""
    phrases.contains("The actor urn:actor:RESTART_ERROR_HANDLER has sent the message: \"Actor urn:actor:RESTART_ERROR_HANDLER started!\"")

    and: "The printed phrases should contain the phrase: \"The actor urn:actor:DEFAULT_ERROR_HANDLER has sent the message: \"Actor urn:actor:DEFAULT_ERROR_HANDLER started!\"\""
    phrases.contains("The actor urn:actor:DEFAULT_ERROR_HANDLER has sent the message: \"Actor urn:actor:DEFAULT_ERROR_HANDLER started!\"")

    and: "The printed phrases should contain the phrase: \"The actor urn:actor:SYSTEM.DEAD_LETTER has sent the message: \"Message \"Hello world!\" from urn:actor:SYSTEM.DEAD_LETTER has been completed with error.\"\""
    phrases.contains("The actor urn:actor:SYSTEM.DEAD_LETTER has sent the message: \"Message \"Hello world!\" from urn:actor:SYSTEM.DEAD_LETTER has been completed with error.\"")

    and: "The printed phrases should contain the phrase: \"The actor urn:actor:BOB has sent the message: \"Message \"Nice to meet you!\" from urn:actor:BOB has been completed with error.\"\""
    phrases.contains("The actor urn:actor:BOB has sent the message: \"Message \"Nice to meet you!\" from urn:actor:BOB has been completed with error.\"")

    and: "The printed phrases should contain the phrase: \"The actor urn:actor:ALICE has sent the message: \"Message \"Nice to meet you too!\" from urn:actor:ALICE has been completed with error.\"\""
    phrases.contains("The actor urn:actor:ALICE has sent the message: \"Message \"Nice to meet you too!\" from urn:actor:ALICE has been completed with error.\"")

    and: "The printed phrases should contain the phrase: \"The actor urn:actor:RESTART_ERROR_HANDLER has sent the message: \"Actor urn:actor:RESTART_ERROR_HANDLER is going to be restarted!\"\""
    phrases.contains("The actor urn:actor:RESTART_ERROR_HANDLER has sent the message: \"Actor urn:actor:RESTART_ERROR_HANDLER is going to be restarted!\"")

    and: "The printed phrases should contain the phrase: \"The actor urn:actor:RESTART_ERROR_HANDLER has sent the message: \"Actor urn:actor:RESTART_ERROR_HANDLER restarted!\"\""
    phrases.contains("The actor urn:actor:RESTART_ERROR_HANDLER has sent the message: \"Actor urn:actor:RESTART_ERROR_HANDLER restarted!\"")

    and: "The printed phrases should contain the phrase: \"Printer is stopped!\""
    phrases.contains("Printer is stopped!") == true
  }

  def "Scenario: register actor with the same name twice"() {
    given: "The actors system"
    FixedMessagesWaitingDispatcher dispatcher = FixedMessagesWaitingDispatcher.singleThreadDispatcher(11)
    ActorSystem actorSystem = createActorSystem(dispatcher)

    and: "The actor system is started"
    actorSystem.start()

    and: "The printer actor is already registered"
    StringWriter printWriter = new StringWriter();
    actorSystem.actorOf(PrinterActor.PRINT_ACTOR_NAME, PrinterActor.factory(printWriter))

    when: "The actor with the same name is registered twice"
    actorSystem.actorOf(PrinterActor.PRINT_ACTOR_NAME, PrinterActor.factory(printWriter))

    then: "The actor has already been created error should be thrown"
    thrown(ActorDuplicationException)

    cleanup:
    actorSystem.shutdown()
  }

  def "Scenario: create and destroy derived actor"() {
    given: "The actors system"
    FixedMessagesWaitingDispatcher dispatcher = FixedMessagesWaitingDispatcher.singleThreadDispatcher(3)
    ActorSystem actorSystem = createActorSystem(dispatcher)

    and: "The actor system is started"
    actorSystem.start()

    and: "The string buffer to which the actor will write input values"
    StringWriter printWriter = new StringWriter();

    and: "The registered printer actor, printing message \"The actor <actor urn> has sent the message: <input message>\""
    ActorAddress actorAddress = actorSystem.actorOf(PrinterLocalUsingActor.PRINTER_LOCAL_USER_ACTOR_NAME, PrinterLocalUsingActor.factory(printWriter))

    when: "The printing command is told to the actor"
    actorSystem.tell(Message.createFor(actorAddress, new PrintLocal()))

    and: "The actor system is stopped after invocation"
    dispatcher.getWaiter().await()
    actorSystem.shutdown()

    then: "The printed phrases should contain the phrase: \"Printer is started!\""
    Collection phrases = Arrays.asList(printWriter.getBuffer().toString().split("\r\n"))
    phrases.contains("Printer is started!") == true

    and: "The printed phrases should contain the phrase: \"Printer is going to be restarted!\""
    phrases.contains("Printer is going to be restarted!") == true

    and: "The printed phrases should contain the phrase: \"Printer is restarted!\""
    phrases.contains("Printer is restarted!") == true


    and: "The printed phrases should contain the phrase: \"The actor urn:actor:PRINTER_LOCAL_USER has sent the message: \"Hello world!\"\""
    phrases.contains("The actor urn:actor:PRINTER_LOCAL_USER has sent the message: \"Hello world!\"") == true

    and: "The printed phrases should contain the phrase: \"The actor urn:actor:PRINTER_LOCAL_USER has sent the message: \"Good bye!\"\""
    phrases.contains("The actor urn:actor:PRINTER_LOCAL_USER has sent the message: \"Good bye!\"") == true

    and: "The printed phrases should contain the phrase: \"Printer is stopped!\""
    phrases.contains("Printer is stopped!") == true
  }

  static class PrinterActor extends Actor<String> {
    public static final ActorName PRINT_ACTOR_NAME = ActorName.wrap("PRINT_ACTOR")
    private PrintWriter printWriter;

    private PrinterActor(Context context, StringWriter stringWriter) {
      super(context);
      this.printWriter = new PrintWriter(stringWriter)
    }


    @Override
    protected void beforeStart() {
      printWriter.println("Printer is started!")
      super.beforeStart()
    }

    @Override
    protected void afterStop() {
      printWriter.println("Printer is stopped!")
      super.afterStop()
    }



    @Override
    protected void beforeRestart() {
      printWriter.println("Printer is going to be restarted!")
      super.beforeRestart()
    }


    @Override
    protected void afterRestart() {
      printWriter.println("Printer is restarted!")
      super.afterRestart();
    }


    @Override
    protected void receive(Message<String> message) {
      System.out.println(message)
      printWriter.println(String.format("The actor %s has sent the message: \"%s\"", message.getSender(), message.getBody()))
    }

    public static final Factory<String> factory(StringWriter bufferWriter) {
      return {Context context -> new PrinterActor(context, bufferWriter)}
    }
  }

  static class ErrorProducerActor extends Actor<Object> {
    private static final ActorName RESUME_STRATEGY_ERROR_HANDLER_ADDRESS = ActorName.wrap("RESUME_ERROR_HANDLER")
    private static final ActorName RESTART_STRATEGY_ERROR_HANDLER_ADDRESS = ActorName.wrap("RESTART_ERROR_HANDLER")
    private static final ActorName DEFAULT_STRATEGY_ERROR_HANDLER_ADDRESS = ActorName.wrap("DEFAULT_ERROR_HANDLER")

    private Optional<FaultResolver<Object>> faultResolverOptional;
    private ActorAddress printerActorAddress;

    public ErrorProducerActor(ActorAddress printerActorAddress, Context context, Optional<FaultResolver<Object>> faultResolver) {
      super(context);
      this.faultResolverOptional = faultResolver;
      this.printerActorAddress = printerActorAddress;
    }

    @Override
    protected void beforeStart() {
      tellAboutLifecyclePhase("Actor %s started!")
      super.beforeStart()
    }

    @Override
    protected void afterStop() {
      tellAboutLifecyclePhase("Actor %s stopped!")
      super.afterStop()
    }

    @Override
    protected void beforeRestart() {
      tellAboutLifecyclePhase("Actor %s is going to be restarted!")
      super.beforeRestart()
    }

    @Override
    protected void afterRestart() {
      tellAboutLifecyclePhase("Actor %s restarted!")
      super.afterRestart()
    }

    private void tellAboutLifecyclePhase(String formatMessage) {
      tell(Message.createFor(self(), printerActorAddress, String.format(formatMessage, self())))
    }

    @Override
    protected void receive(Message<Object> message) {
      throw new RuntimeException(String.format("Message \"%s\" from %s has been completed with error.", message.getBody(), message.getSender()))
    }

    @Override
    protected FaultResolver<Object> getFaultResover() {
      FaultResolver<Object> defaultResolver = super.getFaultResover();
      return new FaultResolver() {
            @Override
            public void resolveError(LifecycleController lifecycle, Message message, Throwable error) {
              tell(Message.createFor(message.getSender(), printerActorAddress, error.getMessage()))
              FaultResolver<Object> faultResolver = faultResolverOptional.orElse(defaultResolver)
              faultResolver.resolveError(lifecycle, message, error);
            }
          }
    }

    private static final Factory<Object> factory(ActorAddress printerActorAddress, Optional<FaultResolver<Object>> faultResolver) {
      return {Context context -> new ErrorProducerActor(printerActorAddress, context, faultResolver)}
    }

    public static final Factory<Object> resumeErrorHandlingStrategyActor(ActorAddress printerActorAddress) {
      return factory(printerActorAddress, Optional.of({LifecycleController lifecycle, Message<Object> message, Throwable error ->
        lifecycle.resume()
      }))
    }

    public static final Factory<Object> restartErrorHandlingStrategyActor(ActorAddress printerActorAddress) {
      return factory(printerActorAddress, Optional.of({LifecycleController lifecycle, Message<Object> message, Throwable error ->
        lifecycle.restart()
      }))
    }

    public static final Factory<Object> defaultErrorHandlingStrategyActor(ActorAddress printerActorAddress) {
      return factory(printerActorAddress, Optional.empty())
    }
  }

  static class PrinterLocalUsingActor extends Actor<PrintLocal> {
    public static final ActorName PRINTER_LOCAL_USER_ACTOR_NAME = ActorName.wrap("PRINTER_LOCAL_USER")

    private final StringWriter bufferWriter;
    private final Factory<String> printerFactory;
    private ActorAddress printerAddress;

    public PrinterLocalUsingActor(Context context, StringWriter bufferWriter) {
      super(context);
      this.bufferWriter = bufferWriter;
      this.printerFactory = PrinterActor.factory(bufferWriter)
    }

    @Override
    protected void receive(Message<PrintLocal> message) {
      this.printerAddress = actorOf(PrinterActor.PRINT_ACTOR_NAME, printerFactory)
      tell(Message.createFor(self(), printerAddress, "Hello world!"))
      restart(printerAddress)
      tell(Message.createFor(self(), printerAddress, "Good bye!"))
    }

    @Override
    protected void afterStop() {
      stop(printerAddress)
    }

    public static final Factory<PrintLocal> factory(StringWriter bufferWriter) {
      return {Context context -> new PrinterLocalUsingActor(context, bufferWriter)}
    }

    static class PrintLocal {
    }
  }

  private ActorSystem createActorSystem(Dispatcher dispatcher) {
    return Actors.create({SystemConfigurer configurer ->
      return configurer
          .withDispatcher(dispatcher)
          .withCorrelationKeyGenerator({GENERATED_KEY})
          .configure()
    })
  }
}
