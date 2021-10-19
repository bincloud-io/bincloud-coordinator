package io.bcs.common.port.adapters.io.transfer.transmitter;

import java.nio.ByteBuffer;

import io.bcs.common.domain.model.io.transfer.CompletionCallback;
import io.bcs.common.domain.model.io.transfer.DestinationPoint;
import io.bcs.common.domain.model.io.transfer.SourcePoint;
import io.bcs.common.domain.model.io.transfer.Transmitter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectTransmitter implements Transmitter {
	private final SourcePoint source;
	private final DestinationPoint destination;
	private final CompletionCallback  completionCallback;
	private TransmissionAction currentAction = new ReceiveAction();
	
	
	@Override
	public void submit(ByteBuffer buffer, Long count) {
		currentAction = new SubmitAction(buffer, count);
	}

	@Override
	public void receive() {
		currentAction = new ReceiveAction();
	}
	

	@Override
	public void start() {
		TransmissionAction step;
		do {
			step = this.currentAction;
			try {
				step.process();				
			} catch (Exception error) {
				fail(error);
				continue;
			}
		} while (!step.isTerminated());
	}

	@Override
	public void complete() {
		currentAction = new SuccessAction();
	}
	
	private void fail(Exception error) {
		currentAction = new FailAction(error);
	}
	
	private void dispose() {
		source.dispose();
		destination.dispose();
	}
	
	private interface TransmissionAction {
		public boolean isTerminated();
		public void process() throws Exception;
	}
	
	@RequiredArgsConstructor
	private class SubmitAction implements TransmissionAction {
		private final ByteBuffer buffer;
		private final Long count;

		@Override
		public boolean isTerminated() {
			return false;
		}

		@Override
		public void process() {
			destination.write(DirectTransmitter.this, buffer, count);
		}
	}
	
	private class ReceiveAction implements TransmissionAction {
		@Override
		public boolean isTerminated() {
			return false;
		}

		@Override
		public void process() {
			source.read(DirectTransmitter.this);
		}
	}
	
	@NoArgsConstructor
	private class SuccessAction implements TransmissionAction {
		@Override
		public boolean isTerminated() {
			return true;
		}

		@Override
		public void process() {
			dispose();
			completionCallback.onSuccess();
		}
	}
	
	@RequiredArgsConstructor
	private class FailAction implements TransmissionAction {
		private final Exception error;
		
		@Override
		public boolean isTerminated() {
			return true;
		}

		@Override
		public void process() {
			dispose();
			completionCallback.onError(error);
		}
	}
}
