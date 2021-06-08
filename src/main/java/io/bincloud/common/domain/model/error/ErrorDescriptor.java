package io.bincloud.common.domain.model.error;

import java.util.Map;

/**
 * This interface declares the contract allowing to get information about error.
 * So it provides bounded context, error code assigned to corresponding bounded
 * context and error details, providing the map of additional properties.
 * 
 * @author Dmitry Mikhaylenko
 *
 */
public interface ErrorDescriptor {
	/**
	 * Get the bounded context name.
	 * 
	 * @return The bounded context name
	 */
	public String getContext();
	
	/**
	 * Get the error code. Must be not null
	 * and non zero value
	 * 
	 * @return The error code
	 */
	public Long getErrorCode();
	
	/**
	 * Get the error additional details map
	 *  
	 * @return The error additional details
	 */
	public Map<String, Object> getDetails();
}
