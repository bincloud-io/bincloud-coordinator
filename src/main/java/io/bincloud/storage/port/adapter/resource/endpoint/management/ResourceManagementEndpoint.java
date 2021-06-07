package io.bincloud.storage.port.adapter.resource.endpoint.management;

import javax.inject.Inject;
import javax.jws.WebService;

import io.bincloud.common.domain.model.error.ApplicationException;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.storage.domain.model.resource.ResourceManager;
import io.bincloud.storage.port.adapter.ServerContextProvider;

@WebService(serviceName = "ResourceManagementService", endpointInterface = "io.bincloud.storage.port.adapter.resource.endpoint.management.ResourceManagementService")
public class ResourceManagementEndpoint implements ResourceManagementService {
	@Inject
	private ResourceManager resourceManager;

	@Inject
	private MessageProcessor messageProcessor;

	@Inject
	private ServerContextProvider serverContext;

	@Override
	public CreateNewResourceRsType createNewResource(CreateNewResourceRqType request) throws CreateNewResourceFault {
		try {
			Long resourceId = resourceManager.createNewResource(new ResourceCreationRequestDetails(request));
			return new ResourceCreationResponse(serverContext.getRootURL(), resourceId);
		} catch (ApplicationException error) {
			throw new ResourceCreationOperationException(messageProcessor, error);
		} catch (Exception error) {
			throw new ResourceCreationOperationException(messageProcessor, error);
		}
	}
}
