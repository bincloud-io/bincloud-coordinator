package io.bce.interaction

import java.util.concurrent.CountDownLatch

import io.bce.interaction.Promise
import io.bce.interaction.Promise.Deferred
import io.bce.interaction.Promise.ErrorHandler
import io.bce.interaction.Promise.PromiseHasAlreadyBeenRejectedException
import io.bce.interaction.Promise.PromiseHasAlreadyBeenResolvedException
import io.bce.interaction.Promise.ResponseHandler
import spock.lang.Specification

class PromiseSpec extends Specification {
	private static final String RESPONSE = "HELLO WORLD!"
	private static final RuntimeException TYPED_EXCEPTION = new RuntimeException("THE TYPED ERROR!")
	private static final Exception UNTYPED_EXCEPTION = new Exception("THE UNTYPED ERROR!")

	def "Scenario: resolve promise"() {
		CountDownLatch latch = new CountDownLatch(1);

		given: "The resolve handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)

		when: "The promise is created"
		Promise<String> promise = new Promise({Deferred<String> resolver ->
			System.sleep(1000)
			resolver.resolve(RESPONSE)
			latch.countDown()
		}).then(firstResponseHandler)
		.error(RuntimeException, firstErrorHandler)
		.error(secondErrorHandler)
		latch.await()

		and: "The response handler is added after invocation"
		promise.then(secondResponseHandler)

		then: "The response handlers should be notified"
		1 * firstResponseHandler.onResponse(RESPONSE)
		1 * secondResponseHandler.onResponse(RESPONSE)

		and: "The error handler shouldn't be notified"
		0 * firstErrorHandler.onError(_)
		0 * secondErrorHandler.onError(_)
	}

	def "Scenario: reject promise with typed error handler"() {
		CountDownLatch latch = new CountDownLatch(1);

		given: "The resolve handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The promise is created"
		Promise<String> promise = new Promise({Deferred<String> resolver ->
			System.sleep(1000)
			resolver.reject(TYPED_EXCEPTION)
			latch.countDown()
		}).then(firstResponseHandler)
		.then(secondResponseHandler)
		.error(RuntimeException, firstErrorHandler)
		.error(secondErrorHandler)
		latch.await()

		and: "The error handler is added after invocation"
		promise.error(RuntimeException, thirdErrorHandler)

		then: "The response handlers should be notified"
		0 * firstResponseHandler.onResponse(_)
		0 * secondResponseHandler.onResponse(_)

		and: "The error handler shouldn't be notified"
		1 * firstErrorHandler.onError(TYPED_EXCEPTION)
		1 * secondErrorHandler.onError(TYPED_EXCEPTION)
		1 * thirdErrorHandler.onError(TYPED_EXCEPTION)
	}

	def "Scenario: resolve promise with untyped error handlers"() {
		CountDownLatch latch = new CountDownLatch(1);

		given: "The resolve handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The promise is created"
		Promise<String> promise = new Promise({Deferred<String> resolver ->
			System.sleep(1000)
			resolver.reject(UNTYPED_EXCEPTION)
			latch.countDown()
		}).then(firstResponseHandler)
		.then(secondResponseHandler)
		.error(RuntimeException, firstErrorHandler)
		.error(RuntimeException, thirdErrorHandler)
		.error(secondErrorHandler)
		latch.await()

		then: "The response handlers should be notified"
		0 * firstResponseHandler.onResponse(_)
		0 * secondResponseHandler.onResponse(_)

		and: "The error handler shouldn't be notified"
		0 * firstErrorHandler.onError(UNTYPED_EXCEPTION)
		1 * secondErrorHandler.onError(UNTYPED_EXCEPTION)
		0 * thirdErrorHandler.onError(UNTYPED_EXCEPTION)
	}

	def "Scenario: resolve twice"() {
		CountDownLatch latch = new CountDownLatch(1);
		PromiseHasAlreadyBeenResolvedException thrownError;
		given: "The resolve handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The promise is created and it is resolved twice"
		Promise<String> promise = new Promise({Deferred<String> resolver ->
			try {
				resolver.resolve(RESPONSE)
				resolver.resolve(RESPONSE)
			} catch (PromiseHasAlreadyBeenResolvedException error) {
				thrownError = error
			} finally {
				latch.countDown()
			}
		})
		
		latch.await()

		then: "The promise has already been resolved exception should be thrown"
		thrownError.getMessage() == "Promise has already been resolved"
	}

	def "Scenario: reject twice"() {
		CountDownLatch latch = new CountDownLatch(1);
		PromiseHasAlreadyBeenRejectedException thrownError;
		given: "The resolve handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The promise is created and it is rejected twice"
		Promise<String> promise = new Promise({Deferred<String> resolver ->
			try {
				resolver.reject(UNTYPED_EXCEPTION)
				resolver.reject(UNTYPED_EXCEPTION)
			} catch (PromiseHasAlreadyBeenRejectedException error) {
				thrownError = error
			} finally {
				latch.countDown()
			}
		})
		
		latch.await()

		then: "The promise has already been resolved exception should be thrown"
		thrownError.getMessage() == "Promise has already been rejected"
	}

	def "Scenario: reject after resolve"() {
		CountDownLatch latch = new CountDownLatch(1);
		PromiseHasAlreadyBeenResolvedException thrownError;
		given: "The resolve handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The promise is created and it is rejected after resolving"
		Promise<String> promise = new Promise({Deferred<String> resolver ->
			try {
				resolver.resolve(RESPONSE)
				resolver.reject(UNTYPED_EXCEPTION)
			} catch (PromiseHasAlreadyBeenResolvedException error) {
				thrownError = error
			} finally {
				latch.countDown()
			}
		})
		
		latch.await()

		then: "The promise has already been resolved exception should be thrown"
		thrownError.getMessage() == "Promise has already been resolved"
	}

	def "Scenario: resolve after reject"() {
		CountDownLatch latch = new CountDownLatch(1);
		PromiseHasAlreadyBeenRejectedException thrownError;
		given: "The resolve handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The promise is created and it is resolved after rejection"
		Promise<String> promise = new Promise({Deferred<String> resolver ->
			try {
				resolver.reject(UNTYPED_EXCEPTION)
				resolver.resolve(RESPONSE)
			} catch (PromiseHasAlreadyBeenRejectedException error) {
				thrownError = error
			} finally {
				latch.countDown()
			}
		})
		
		latch.await()

		then: "The promise has already been resolved exception should be thrown"
		thrownError.getMessage() == "Promise has already been rejected"
	}
}
