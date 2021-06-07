package io.bincloud.storage.port.adapter.resource.endpoint;

import io.bincloud.common.domain.model.error.ApplicationException;
import io.bincloud.common.domain.model.error.ErrorDescriptionGenerator;
import io.bincloud.common.domain.model.error.UnexpectedSystemBehaviorException;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.common.port.adapter.integration.global.ServiceFaultType;

public class WebServiceFaultInfo extends ServiceFaultType {
	public WebServiceFaultInfo(MessageProcessor messageProcessor, ApplicationException error) {
		super();
		ErrorDescriptionGenerator descriptionGenerator = ErrorDescriptionGenerator.of(messageProcessor, error);
		setSeverity(error.getSeverity().name());
		setBoundedContext(error.getContext());
		setErrorNumber(error.getErrorCode());
		setMessage(descriptionGenerator.generateDescription());
	}

	public WebServiceFaultInfo(MessageProcessor messageProcessor, String boundedContext, Exception error) {
		this(messageProcessor, new UnexpectedSystemBehaviorException(boundedContext, error));
	}
}
