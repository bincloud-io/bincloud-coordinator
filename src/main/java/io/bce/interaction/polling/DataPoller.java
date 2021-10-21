package io.bce.interaction.polling;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class DataPoller<D> implements Iterable<PolledElement<D>> {
	private final BatchPoller<D> dataPoller;
	
	@Override
	public Iterator<PolledElement<D>> iterator() {	
		return new PollIterator();
	}
	
	private class PollIterator implements Iterator<PolledElement<D>> {
		private Iterator<D> polledDataIterator = Collections.emptyIterator();
		private AtomicLong index = new AtomicLong(0L);
		
		@Override
		public boolean hasNext() {
			runPollingIterationStep();
			return polledDataIterator.hasNext();
		}

		@Override
		public PolledElement<D> next() {
			runPollingIterationStep();
			return new PolledElement<D>(index.getAndIncrement(), polledDataIterator.next());
		}
		
		private void runPollingIterationStep() {
			if (isRequirePollingIteration()) {
				pollNext();
			}
		}
		
		private boolean isRequirePollingIteration() {
			return !polledDataIterator.hasNext();
		}
		
		private void pollNext() {
			Collection<D> polledData = dataPoller.poll();
			switchPolledDataIterator(polledData.iterator());
		}
		
		private void switchPolledDataIterator(Iterator<D> polledDataIterator) {
			this.polledDataIterator = polledDataIterator;
		}
	}
}
