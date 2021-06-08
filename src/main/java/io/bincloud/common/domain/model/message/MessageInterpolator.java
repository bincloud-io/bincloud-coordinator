package io.bincloud.common.domain.model.message;

/**
 * This interface defines the contract for message template 
 * interpolation responsibility.
 * 
 * @author Dmitry Mikhaylenko
 *
 */
public interface MessageInterpolator {
	/**
	 * Interpolate the message template to string using template tool
	 * 
	 * @param messageTemplate The message template
	 * @return The interpolated message
	 */
	public String interpolate(MessageTemplate messageTemplate);
}
