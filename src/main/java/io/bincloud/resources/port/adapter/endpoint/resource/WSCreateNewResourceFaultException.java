package io.bincloud.resources.port.adapter.endpoint.resource;

import io.bincloud.common.domain.model.error.ApplicationException;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.resources.domain.model.Constants;
import io.bincloud.resources.port.adapter.endpoint.WebServiceFaultInfo;
import io.bincloud.storage.port.adapter.resource.endpoint.management.CreateNewResourceFault;

public class WSCreateNewResourceFaultException extends CreateNewResourceFault {
	private static final long serialVersionUID = 5810762657534584298L;

	public WSCreateNewResourceFaultException(MessageProcessor messageProcessor, ApplicationException error) {
		super(error.getMessage(), new WebServiceFaultInfo(messageProcessor, error), error);
	}
	
	public WSCreateNewResourceFaultException(MessageProcessor messageProcessor, Exception error) {
		super(error.getMessage(), new WebServiceFaultInfo(messageProcessor, Constants.CONTEXT, error), error);
	}
}
