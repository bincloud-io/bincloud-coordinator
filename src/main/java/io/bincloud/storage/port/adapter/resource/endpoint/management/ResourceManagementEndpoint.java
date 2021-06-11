package io.bincloud.storage.port.adapter.resource.endpoint.management;

import java.util.Optional;

import javax.inject.Inject;
import javax.jws.WebService;

import io.bincloud.common.domain.model.error.ApplicationException;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.common.port.adapter.integration.global.ServiceResponseType;
import io.bincloud.storage.domain.model.resource.facades.ResourceManager;
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

	@Override
	public ServiceResponseType removeExistingResource(RemoveExistingResourceRqType request)
			throws RemoveExistingResourceFault {	
		try {
			resourceManager.removeExistingResource(Optional.of(request.getResourceId()));
			return new ResourceRemovingResponse();
		} catch (ApplicationException error) {
			throw new ResourceRemovingOperationException(messageProcessor, error);
		} catch (Exception error) {
			throw new ResourceRemovingOperationException(messageProcessor, error);
		}	
	}
}
