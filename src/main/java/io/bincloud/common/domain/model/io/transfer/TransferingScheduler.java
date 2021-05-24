package io.bincloud.common.domain.model.io.transfer;

/**
 * This interface declares the contract for data transferring scheduling
 * 
 * @author Dmitry Mikhaylenko
 *
 */
public interface TransferingScheduler {
	/**
	 * Schedule data transferring from source to destination
	 *  
	 * @param source The source point
	 * @param destination The destination point
	 * @param completionCallback The completion callback
	 * @return The data transmitter
	 */
	public Transmitter schedule(SourcePoint source, DestinationPoint destination,
			CompletionCallback completionCallback);
}
