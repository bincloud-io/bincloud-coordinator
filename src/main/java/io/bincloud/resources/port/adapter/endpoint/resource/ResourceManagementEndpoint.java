package io.bincloud.resources.port.adapter.endpoint.resource;

import javax.inject.Inject;
import javax.jws.WebService;

import io.bincloud.common.domain.model.error.ApplicationException;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.common.port.adapter.integration.global.ServiceResponseType;
import io.bincloud.resources.domain.model.resource.ResourceManager;
import io.bincloud.resources.port.adapter.ServerContextProvider;
import io.bincloud.storage.port.adapter.resource.endpoint.management.CreateNewResourceFault;
import io.bincloud.storage.port.adapter.resource.endpoint.management.CreateNewResourceRqType;
import io.bincloud.storage.port.adapter.resource.endpoint.management.CreateNewResourceRsType;
import io.bincloud.storage.port.adapter.resource.endpoint.management.RemoveExistingResourceFault;
import io.bincloud.storage.port.adapter.resource.endpoint.management.RemoveExistingResourceRqType;
import io.bincloud.storage.port.adapter.resource.endpoint.management.ResourceManagementService;

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
			Long resourceId = resourceManager.createNewResource(new WSCreateNewResourceCommand(request));
			return new WSCreateNewResourceResponse(serverContext.getRootURL(), resourceId);
		} catch (ApplicationException error) {
			throw new WSCreateNewResourceFaultException(messageProcessor, error);
		} catch (Exception error) {
			throw new WSCreateNewResourceFaultException(messageProcessor, error);
		}
	}

	@Override
	public ServiceResponseType removeExistingResource(RemoveExistingResourceRqType request)
			throws RemoveExistingResourceFault {	
		try {
			resourceManager.removeExistingResource(new WSRemoveExistingResource(request));
			return new WSRemoveExistingResourceResponse();
		} catch (ApplicationException error) {
			throw new WSRemoveExistingResourceFaultException(messageProcessor, error);
		} catch (Exception error) {
			throw new WSRemoveExistingResourceFaultException(messageProcessor, error);
		}	
	}
}
