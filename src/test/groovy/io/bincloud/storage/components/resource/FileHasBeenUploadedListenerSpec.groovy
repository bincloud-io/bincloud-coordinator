package io.bincloud.storage.components.resource

import io.bincloud.common.event.EventListener
import io.bincloud.storage.domain.model.resource.FileHasBeenUploaded
import io.bincloud.storage.domain.model.resource.file.FileHasBeenUploadedListener
import io.bincloud.storage.domain.model.resource.file.FileUploading
import io.bincloud.storage.domain.model.resource.file.FileUploadingRepository
import spock.lang.Specification

class FileHasBeenUploadedListenerSpec extends Specification {
	private static final Long RESOURCE_ID = 1L;
	private static final String FILE_ID = "12345";
	
	private FileUploadingRepository fileUploadingRepository
	private EventListener<FileHasBeenUploaded> eventListener
	
	def setup() {
		this.fileUploadingRepository = Mock(FileUploadingRepository)
		this.eventListener = new FileHasBeenUploadedListener(fileUploadingRepository)
	}
	
	def "Scenario: the file has been uploaded event has been published"() {
		
		given: "The file has been uploaded event"
		FileHasBeenUploaded event = new FileHasBeenUploaded(RESOURCE_ID, FILE_ID)
		 
		when: "The file has been uploaded event has been published"
		eventListener.onEvent(event)
		
		then: "The file uploading have been saved"
		1 * fileUploadingRepository.save(_) >> {
			FileUploading fileUploading = it[0];
			fileUploading.resourceId == RESOURCE_ID
			fileUploading.fileId == FILE_ID 
		}		
	}
}
