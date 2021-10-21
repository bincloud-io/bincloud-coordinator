package io.bce.interaction.polling;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Polling {
	public final <D> Stream<D> sequentialPolling(BatchPoller<D> pollingFunction) {
		return sequentialNumeratedPolling(pollingFunction).map(PolledElement::getData);
	}
	
	public final <D> Stream<D> parallelPolling(BatchPoller<D> pollingFunction) {
		return parallelNumeratedPolling(pollingFunction).map(PolledElement::getData);
	}
	
	public final <D> Stream<PolledElement<D>> sequentialNumeratedPolling(BatchPoller<D> pollingFunction) {
		return polling(pollingFunction, false);
	}
	
	public final <D> Stream<PolledElement<D>> parallelNumeratedPolling(BatchPoller<D> pollingFunction) {
		return polling(pollingFunction, true);
	}
	
	private final <D> Stream<PolledElement<D>> polling(BatchPoller<D> pollingFunction, boolean parallel) {
		DataPoller<D> dataPoller = new DataPoller<D>(pollingFunction);
		return StreamSupport.stream(dataPoller.spliterator(), parallel);
	}
}
