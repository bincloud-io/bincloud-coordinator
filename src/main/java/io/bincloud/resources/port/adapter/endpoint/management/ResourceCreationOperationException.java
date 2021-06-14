package io.bincloud.resources.port.adapter.endpoint.management;

import io.bincloud.common.domain.model.error.ApplicationException;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.resources.domain.model.Constants;
import io.bincloud.resources.port.adapter.endpoint.WebServiceFaultInfo;
import io.bincloud.storage.port.adapter.resource.endpoint.management.CreateNewResourceFault;

public class ResourceCreationOperationException extends CreateNewResourceFault {
	private static final long serialVersionUID = 5810762657534584298L;

	public ResourceCreationOperationException(MessageProcessor messageProcessor, ApplicationException error) {
		super(error.getMessage(), new WebServiceFaultInfo(messageProcessor, error), error);
	}
	
	public ResourceCreationOperationException(MessageProcessor messageProcessor, Exception error) {
		super(error.getMessage(), new WebServiceFaultInfo(messageProcessor, Constants.CONTEXT, error), error);
	}
}
