package io.bincloud.resources.domain.model.resource;

import io.bincloud.common.domain.model.validation.ValidationException;
import io.bincloud.resources.domain.model.FileReference;

public interface ResourceManager {
	/**
	 * Create new resource. It creates resource entity and publish
	 * {@link ResourceHasBeenCreated} if it is successfully created
	 * 
	 * @param command The resource creating command
	 * @throws ValidationException if command state is invalid
	 * @return The generated resource identifier
	 */
	public Long createNewResource(CreateResource command);
	
	/**
	 * Remove existing resource. It removes resource and publish
	 * {@link ResourceHasBeenDeleted} if it is successfully removed
	 * 
	 * @param command The resource deleting command
	 * @throws ValidationException if command state is invalid
	 * @throws ResourceDoesNotExistException if resource does not exists
	 */
	public void removeExistingResource(RemoveExistingResource command);
	
	/**
	 * Create new upload link. It creates parameters for upload URL generation
	 * 
	 * @param command The file upload URL generating command
	 * @throws ValidationException if state is invalid
	 * @throws ResourceDoesNotExistException if resource does not exists
	 * @return The file reference parameters which are used for upload address generating
	 */
	public FileReference createUploadLink(CreateUploadLink command);
}
