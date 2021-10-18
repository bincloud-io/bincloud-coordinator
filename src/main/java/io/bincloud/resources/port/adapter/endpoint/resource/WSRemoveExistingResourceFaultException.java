package io.bincloud.resources.port.adapter.endpoint.resource;

import io.bincloud.common.domain.model.error.ApplicationException;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.resources.domain.model.Constants;
import io.bincloud.resources.port.adapter.endpoint.WebServiceFaultInfo;
import io.bincloud.storage.port.adapter.resource.endpoint.management.RemoveExistingResourceFault;

public class WSRemoveExistingResourceFaultException extends RemoveExistingResourceFault {
	private static final long serialVersionUID = 7059253781679830492L;

	public WSRemoveExistingResourceFaultException(MessageProcessor messageProcessor, ApplicationException error) {
		super(error.getMessage(), new WebServiceFaultInfo(messageProcessor, error), error);
	}
	
	public WSRemoveExistingResourceFaultException(MessageProcessor messageProcessor, Exception error) {
		super(error.getMessage(), new WebServiceFaultInfo(messageProcessor, Constants.CONTEXT, error), error);
	}
}
