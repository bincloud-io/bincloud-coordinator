package io.bcs.common.domain.model.io

import java.nio.ByteBuffer

import io.bcs.common.domain.model.error.ApplicationException
import io.bcs.common.domain.model.error.ApplicationException.Severity
import io.bcs.common.domain.model.io.InputOutputException
import io.bcs.common.domain.model.io.transfer.CompletionCallback
import io.bcs.common.domain.model.io.transfer.DataTransferingException
import io.bcs.common.domain.model.io.transfer.DestinationPoint
import io.bcs.common.domain.model.io.transfer.SourcePoint
import io.bcs.common.domain.model.io.transfer.TransferingScheduler
import io.bcs.common.domain.model.io.transfer.Transmitter
import io.bcs.common.port.adapters.io.transfer.destinations.CloseOnDisposeStreamDestination
import io.bcs.common.port.adapters.io.transfer.destinations.StreamDestination
import io.bcs.common.port.adapters.io.transfer.sources.CloseOnDisposeStreamSource
import io.bcs.common.port.adapters.io.transfer.sources.StreamSource
import io.bcs.common.port.adapters.io.transfer.transmitter.DirectTransferingScheduler
import spock.lang.Specification

class DirectTransmissionSpec extends Specification {
	private static final TRANSFERRING_DATA = "Hello World!!! Hello World!!! Hello World!!! Hello World!!!"

	def "Scenario: transfer data based on streams"() {
		CompletionCallback completionCallback = Mock(CompletionCallback);
		given: "The input stream based source point"
		CloseableInputStream inputStream = new CloseableInputStream(TRANSFERRING_DATA.bytes);
		StreamSource sourcePoint = new StreamSource(inputStream, 10);

		and: "The output stream based destination poing"
		CloseableOutputStream outputStream = new CloseableOutputStream();
		StreamDestination destinationPoint = new StreamDestination(outputStream)

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
		
		and: "The input stream and output stream shouldn't be closed"
		inputStream.isClosed() == false
		outputStream.isClosed() == false
	}
	
	def "Scenario: transfer data based on streams with closing on dispose"() {
		CompletionCallback completionCallback = Mock(CompletionCallback);
		given: "The input stream based source point"
		CloseableInputStream inputStream = new CloseableInputStream(TRANSFERRING_DATA.bytes);
		SourcePoint sourcePoint = new CloseOnDisposeStreamSource(inputStream, 10);

		and: "The output stream based destination poing"
		CloseableOutputStream outputStream = new CloseableOutputStream();
		DestinationPoint destinationPoint = new CloseOnDisposeStreamDestination(outputStream)

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
		
		and: "The input stream and output stream shouldn't be closed"
		inputStream.isClosed() == true
		outputStream.isClosed() == true
	}
	
	private class CloseableInputStream extends ByteArrayInputStream {
		private boolean closed;
		
		public CloseableInputStream(byte[] buf) {
			super(buf);
			closed = false;
		}
		
		public boolean isClosed() {
			return closed;
		}

		@Override
		public void close() throws IOException {
			super.close();
			closed = true;
		}
	}
	
	private class CloseableOutputStream extends ByteArrayOutputStream {
		private boolean closed;
		
		public CloseableOutputStream() {
			super();
			closed = false;
		}

		public boolean isClosed() {
			return closed;
		}

		@Override
		public void close() throws IOException {
			super.close();
			closed = true;
		}
	}

	def "Scenario: the source point has thrown data transfering exception"() {
		ApplicationException receivedError
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
		1 * completionCallback.onError(_) >> {receivedError = it[0]}
		receivedError.context == InputOutputException.CONTEXT
		receivedError.severity == Severity.INCIDENT
		receivedError.errorCode == DataTransferingException.ERROR_CODE
	}

	def "Scenario: the destination point has thrown data transfering exception"() {
		ApplicationException receivedError;
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
		1 * completionCallback.onError(_) >> {receivedError = it[0]}
		receivedError.context == InputOutputException.CONTEXT
		receivedError.severity == Severity.INCIDENT
		receivedError.errorCode == DataTransferingException.ERROR_CODE
	}

	def "Scenario: input stream reading error during data transferring"() {
		ApplicationException receivedError;
		CompletionCallback completionCallback = Mock(CompletionCallback);
		given: "The wrong opened input stream"
		InputStream inputStream = Mock(InputStream)
		SourcePoint sourcePoint = new StreamSource(inputStream, 10);
		inputStream.read(_, _, _) >> {throw new IOException()}

		and: "The output stream based destination poing"
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		DestinationPoint destinationPoint = new StreamDestination(outputStream)

		and: "The direct transmitter scheduler"
		TransferingScheduler scheduler = new DirectTransferingScheduler()

		when: "We schedule transfering"
		Transmitter transmitter = scheduler.schedule(sourcePoint, destinationPoint, completionCallback)

		and: "We start data transferring"
		transmitter.start()

		then: "The data transferring should be completed with data transferring error"
		1 * completionCallback.onError(_) >> {receivedError = it[0]}
		receivedError.context == InputOutputException.CONTEXT
		receivedError.severity == Severity.INCIDENT
		receivedError.errorCode == DataTransferingException.ERROR_CODE
	}

	def "Scenario: output stream writing error during data transferring"() {
		ApplicationException receivedError;
		CompletionCallback completionCallback = Mock(CompletionCallback);
		given: "The input stream based source point"
		ByteArrayInputStream inputStream = new ByteArrayInputStream(TRANSFERRING_DATA.bytes);
		SourcePoint sourcePoint = new StreamSource(inputStream, 10);

		and: "The wrong opened output stream"
		OutputStream outputStream = Mock(OutputStream)
		DestinationPoint destinationPoint = new StreamDestination(outputStream)
		outputStream.write(_, _, _) >> {throw new IOException()}

		and: "The direct transmitter scheduler"
		TransferingScheduler scheduler = new DirectTransferingScheduler()

		when: "We schedule transfering"
		Transmitter transmitter = scheduler.schedule(sourcePoint, destinationPoint, completionCallback)

		and: "We start data transferring"
		transmitter.start()

		then: "The data transferring should be completed with data transferring error"
		1 * completionCallback.onError(_) >> {receivedError = it[0]}
		receivedError.context == InputOutputException.CONTEXT
		receivedError.severity == Severity.INCIDENT
		receivedError.errorCode == DataTransferingException.ERROR_CODE
	}
}
