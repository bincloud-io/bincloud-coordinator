package io.bincloud.common.components.io

import java.nio.ByteBuffer

import io.bincloud.common.ApplicationException
import io.bincloud.common.ApplicationException.Severity
import io.bincloud.common.io.InputOutputException
import io.bincloud.common.io.transfer.CompletionCallback
import io.bincloud.common.io.transfer.DataTransferingException
import io.bincloud.common.io.transfer.DestinationPoint
import io.bincloud.common.io.transfer.SourcePoint
import io.bincloud.common.io.transfer.TransferingScheduler
import io.bincloud.common.io.transfer.Transmitter
import io.bincloud.common.io.transfer.destinations.StreamDestination
import io.bincloud.common.io.transfer.sources.StreamSource
import io.bincloud.common.io.transfer.transmitter.DirectTransferingScheduler
import spock.lang.Specification

class DirectTransmissionSpec extends Specification {
	private static final TRANSFERRING_DATA = "Hello World!!! Hello World!!! Hello World!!! Hello World!!!"

	def "Scenario: transfer data based on streams"() {
		CompletionCallback completionCallback = Mock(CompletionCallback);
		given: "The input stream based source point"
		ByteArrayInputStream inputStream = new ByteArrayInputStream(TRANSFERRING_DATA.bytes);
		SourcePoint sourcePoint = new StreamSource(inputStream, 10);

		and: "The output stream based destination poing"
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		DestinationPoint destinationPoint = new StreamDestination(outputStream)

		and: "The direct transmitter scheduler"
		TransferingScheduler scheduler = new DirectTransferingScheduler()

		when: "We schedule transfering"
		Transmitter transmitter = scheduler.schedule(sourcePoint, destinationPoint, completionCallback)

		and: "We start data transferring"
		transmitter.start()

		then: "The transferred data will be the same as the source data"
		byte[] transferredData = outputStream.toByteArray()
		TRANSFERRING_DATA.bytes == transferredData

		and: "The system is asynchronously notified about successful ending of transferring process"
		1 * completionCallback.onSuccess()
	}

	def "Scenario: the source point has thrown data transfering exception"() {
		CompletionCallback completionCallback = Mock(CompletionCallback);
		given: "The source point"
		SourcePoint sourcePoint = Mock(SourcePoint)

		and: "The source point will throw data transfering exception on data reading"
		sourcePoint.read(_) >> {throw new DataTransferingException("Something went wrong!!!")}

		and: "The destination point"
		DestinationPoint destinationPoint = Mock(DestinationPoint)

		and: "The direct transmitter scheduler"
		TransferingScheduler scheduler = new DirectTransferingScheduler()

		when: "We schedule transfering"
		Transmitter transmitter = scheduler.schedule(sourcePoint, destinationPoint, completionCallback)

		and: "We start data transferring"
		transmitter.start()

		then: "The both source and destination points has been disposed"
		1 * sourcePoint.dispose()
		1 * destinationPoint.dispose()

		and: "The system is asynchronously notified about error"
		1 * completionCallback.onError(_) >> {arguments ->
			ApplicationException receivedError = arguments[0]
			receivedError.context == InputOutputException.CONTEXT
			receivedError.severity == Severity.INCIDENT
			receivedError.errorCode == DataTransferingException.ERROR_CODE
		}
	}

	def "Scenario: the destination point has thrown data transfering exception"() {
		CompletionCallback completionCallback = Mock(CompletionCallback);
		given: "The source point"
		SourcePoint sourcePoint = Mock(SourcePoint)
		sourcePoint.read(_) >> {arguments -> arguments[0].submit(ByteBuffer.allocate(10), 5L)}

		and: "The destination point"
		DestinationPoint destinationPoint = Mock(DestinationPoint)

		and: "The destination point will throw data transfering exception on data reading"
		destinationPoint.write(_, _, _) >> {throw new DataTransferingException("Something went wrong!!!")}

		and: "The direct transmitter scheduler"
		TransferingScheduler scheduler = new DirectTransferingScheduler()

		when: "We schedule transfering"
		Transmitter transmitter =scheduler.schedule(sourcePoint, destinationPoint, completionCallback)

		and: "We start data transferring"
		transmitter.start()

		then: "The both source and destination points has been disposed"
		1 * sourcePoint.dispose()
		1 * destinationPoint.dispose()

		and: "The system is asynchronously notified about error"
		1 * completionCallback.onError(_) >> {arguments ->
			ApplicationException receivedError = arguments[0]
			receivedError.context == InputOutputException.CONTEXT
			receivedError.severity == Severity.INCIDENT
			receivedError.errorCode == DataTransferingException.ERROR_CODE
		}
	}
}
