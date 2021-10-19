package io.bcs.common.domain.model.message;

import java.util.Map;

/**
 * This interface defines the contract for message template string, which
 * provides message template string and parameters for template tool which will
 * interpolate this message template to the final messate
 * 
 * @author Dmitry Mikhaylenko
 *
 */
public interface MessageTemplate {
	/**
	 * Get the message template text. It might be bundle id, template to text
	 * template processing tool or plain text.
	 * 
	 * @return The message template text
	 */
	public String getText();

	/**
	 * Get the parameters which will be applied to template processing tool
	 * 
	 * @return The parameters map
	 */
	public Map<String, Object> getParameters();
}
