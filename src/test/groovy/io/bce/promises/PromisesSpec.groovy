package io.bce.promises

import java.util.concurrent.CountDownLatch

import io.bce.promises.Promise.ChainingDeferredFunction
import io.bce.promises.Promise.ChainingPromiseHandler
import io.bce.promises.Promise.ErrorHandler
import io.bce.promises.Promise.ResponseHandler
import io.bce.promises.Promises.PromiseHasAlreadyBeenRejectedException
import io.bce.promises.Promises.PromiseHasAlreadyBeenResolvedException
import spock.lang.Specification

class PromisesSpec extends Specification {
	private static final String RESPONSE = "HELLO WORLD!"
	private static final RuntimeException TYPED_EXCEPTION = new RuntimeException("THE TYPED ERROR!")
	private static final Exception UNTYPED_EXCEPTION = new Exception("THE UNTYPED ERROR!")

	def "Scenario: resolve promise"() {
		CountDownLatch latch = new CountDownLatch(1);

		given: "The response handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)

		when: "The deferred operation is executed"
		Promise<String> promise = Promises.of({Deferred<String> resolver ->
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

		given: "The response handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The deferred operation is executed"
		Promise<String> promise = Promises.of({Deferred<String> resolver ->
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

	def "Scenario: reject promise with untyped error handlers"() {
		CountDownLatch latch = new CountDownLatch(1);

		given: "The response handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The deferred operation is executed"
		Promise<String> promise = Promises.of({Deferred<String> resolver ->
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

	def "Scenario: reject promise on thrown exception"() {
		CountDownLatch latch = new CountDownLatch(1);
		RuntimeException error = new RuntimeException()

		given: "The error handlers"
		ErrorHandler<RuntimeException> errorHandler = Mock(ErrorHandler)

		when: "The deferred operation is executed and exception is thrown"
		Promises.of({deferred ->
			throw error;
		}).error({e ->
			errorHandler.onError(e)
			latch.countDown();
		})

		latch.await()

		then: "The promise should be resolved as ususal as on rejected method call"
		1 * errorHandler.onError(error)
	}

	def "Scenario: resolve twice"() {
		CountDownLatch latch = new CountDownLatch(1);
		PromiseHasAlreadyBeenResolvedException thrownError;

		given: "The response handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The deferred operation is executed and it is resolved twice"
		Promise<String> promise = Promises.of({Deferred<String> resolver ->
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

		given: "The response handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The deferred operation is executed and it is rejected twice"
		Promise<String> promise = Promises.of({Deferred<String> resolver ->
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

		given: "The response handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The deferred operation is executed and it is rejected after resolving"
		Promise<String> promise = Promises.of({Deferred<String> resolver ->
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

		given: "The response handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The deferred operation is executed and it is resolved after rejection"
		Promise<String> promise = Promises.of({Deferred<String> resolver ->
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

	def "Scenario: resolve chained promises using chaining deferred function"() {
		CountDownLatch latch = new CountDownLatch(2);

		given: "The response handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		when: "The promises chain is created and all promises are resolved"
		Promises.of({Deferred<Long> deferred ->
			deferred.resolve(100L)
			latch.countDown()
		}).then(firstResponseHandler)
		.chain((ChainingDeferredFunction) {previous, deferred ->
			deferred.resolve("${previous}")
			latch.countDown()
		}).then(secondResponseHandler)

		latch.await()

		then: "The first handler should be resolved by long value"
		1 * firstResponseHandler.onResponse(100L)

		and: "The second handler should be resolved by stringified value"
		1 * secondResponseHandler.onResponse("100")
	}

	def "Scnario: reject chained promise using chaining deferred function if it is rejected"() {
		RuntimeException error = new RuntimeException()
		CountDownLatch latch = new CountDownLatch(2);

		given: "The response handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The promises chain is created and first promise is resolved, but second is failed"
		Promises.of({Deferred<Long> deferred ->
			deferred.resolve(100L)
			latch.countDown()
		}).then(firstResponseHandler)
		.error(RuntimeException, firstErrorHandler)
		.chain((ChainingDeferredFunction) {previous, deferred ->
			deferred.reject(error)
			latch.countDown()
		}).then(secondResponseHandler)
		.error(RuntimeException, secondErrorHandler)
		.error(thirdErrorHandler)

		latch.await()

		then: "The first response handler should be  resolved by long value"
		1 * firstResponseHandler.onResponse(100L)

		and: "The second response handler shouldn't be resolved"
		0 * secondResponseHandler.onResponse(_)

		and: "All error handlers should be called"
		1 * firstErrorHandler.onError(error)
		1 * secondErrorHandler.onError(error)
		1 * thirdErrorHandler.onError(error)
	}
	
	def "Scnario: reject chained promise using chaining deferred function if an error is thrown"() {
		RuntimeException error = new RuntimeException()
		CountDownLatch latch = new CountDownLatch(1);

		given: "The response handler"
		ResponseHandler responseHandler = Mock(ResponseHandler)

		and: "The error handler"
		ErrorHandler<RuntimeException> errorHandler = Mock(ErrorHandler)

		when: "The promises chain is created and first promise is resolved, but second is thrown an error"
		Promises.of({Deferred<Long> deferred ->
			deferred.resolve(100L)
		}).then(responseHandler)
		.chain((ChainingDeferredFunction) {previous, deferred -> throw error})
		.error({ RuntimeException err -> 
			errorHandler.onError(err)
			latch.countDown()
		})
		
		latch.await()

		then: "The response handler should be  resolved by long value"
		1 * responseHandler.onResponse(100L)

		and: "The error handler should be called"
		1 * errorHandler.onError(error)
	}

	def "Scenario: resolve chained promises using chaining promise handler"() {
		CountDownLatch latch = new CountDownLatch(2);

		given: "The response handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		when: "The promises chain is created and all promises are resolved"
		Promises.of({Deferred<Long> deferred ->
			deferred.resolve(100L)
			latch.countDown()
		}).then(firstResponseHandler)
		.chain((ChainingPromiseHandler) {previous ->
			return Promises.of({ deferred ->
				deferred.resolve("${previous}")
			})
		}).then({response ->
			secondResponseHandler.onResponse(response)
			latch.countDown()
		})

		latch.await()

		then: "The first handler should be resolved by long value"
		1 * firstResponseHandler.onResponse(100L)

		and: "The second handler should be resolved by stringified value"
		1 * secondResponseHandler.onResponse("100")
	}

	def "Scnario: reject chained promise using chaining promise handler"() {
		RuntimeException error = new RuntimeException()
		CountDownLatch latch = new CountDownLatch(2);

		given: "The response handlers"
		ResponseHandler firstResponseHandler = Mock(ResponseHandler)
		ResponseHandler secondResponseHandler = Mock(ResponseHandler)

		and: "The error handlers"
		ErrorHandler<RuntimeException> firstErrorHandler = Mock(ErrorHandler)
		ErrorHandler<Throwable> secondErrorHandler = Mock(ErrorHandler)
		ErrorHandler<RuntimeException> thirdErrorHandler = Mock(ErrorHandler)

		when: "The promises chain is created and first promise is resolved, but second is failed"
		Promises.of({Deferred<Long> deferred ->
			deferred.resolve(100L)
			latch.countDown()
		}).then(firstResponseHandler)
		.error(RuntimeException, firstErrorHandler)
		.chain((ChainingPromiseHandler) {previous ->
			return Promises.of({ deferred ->
				deferred.reject(error)
				latch.countDown()
			})
		}).then(secondResponseHandler)
		.error(RuntimeException, secondErrorHandler)
		.error(thirdErrorHandler)
		
		latch.await()

		then: "The first response handler should be  resolved by long value"
		1 * firstResponseHandler.onResponse(100L)

		and: "The second response handler shouldn't be resolved"
		0 * secondResponseHandler.onResponse(_)

		and: "All error handlers should be called on the failed promise only"
		0 * firstErrorHandler.onError(error)
		1 * secondErrorHandler.onError(error)
		1 * thirdErrorHandler.onError(error)
	}
	
	def "Scenario: create the resolved promise by a value"() {
		CountDownLatch latch = new CountDownLatch(1)
		given: "The response handler"
		ResponseHandler responseHandler = Mock(ResponseHandler)
		
		when: "The resolved promise is created"
		Promises.resolvedBy(100L).then({Long value -> 
			responseHandler.onResponse(value)
			latch.countDown()
		})
		
		latch.await()
		
		then: "The response handler should catch the value"
		1 * responseHandler.onResponse(100L)	
	}
	
	def "Scenario: create the rejected promise by an error"() {
		CountDownLatch latch = new CountDownLatch(1)
		RuntimeException error = new RuntimeException("ERROR")
		
		given: "The error handler"
		ErrorHandler errorHandler = Mock(ErrorHandler)
		
		when: "The rejected promise is created"
		Promises.rejectedBy(error).error({RuntimeException err ->
			errorHandler.onError(err)
			latch.countDown()
		})
		
		latch.await()
		
		then: "The error handler should catch the error"
		1 * errorHandler.onError(error)
	}
}
