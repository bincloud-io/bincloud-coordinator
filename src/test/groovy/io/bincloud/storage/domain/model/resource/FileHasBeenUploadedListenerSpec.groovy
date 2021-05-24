package io.bincloud.storage.domain.model.resource

import io.bincloud.common.domain.model.event.EventListener
import io.bincloud.common.domain.model.time.DateTime
import io.bincloud.storage.domain.model.resource.FileHasBeenUploaded
import io.bincloud.storage.domain.model.resource.file.FileHasBeenUploadedListener
import io.bincloud.storage.domain.model.resource.file.FileUploading
import io.bincloud.storage.domain.model.resource.file.FileUploadingRepository
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	According to business rules, when the file has been successfully uploaded,
	we have to bind this one with resource. As a developer, I'm needed in a 
	component, which will listen \"FileHasBeenUploaded\" domain event and 
	bind the file with corresponding resource.
""")
class FileHasBeenUploadedListenerSpec extends Specification {
	private static final Long RESOURCE_ID = 1L;
	private static final String FILE_ID = "12345";
	private static final DateTime UPLOADING_MOMENT = new DateTime()
	
	private FileUploadingRepository fileUploadingRepository
	private EventListener<FileHasBeenUploaded> eventListener
	
	def setup() {
		this.fileUploadingRepository = Mock(FileUploadingRepository)
		this.eventListener = new FileHasBeenUploadedListener(fileUploadingRepository)
	}
	
	def "Scenario: the file has been uploaded event has been published"() {
		
		given: "The file has been uploaded event"
		FileHasBeenUploaded event = new FileHasBeenUploaded(RESOURCE_ID, FILE_ID, UPLOADING_MOMENT)
		 
		when: "The file has been uploaded event has been published"
		eventListener.onEvent(event)
		
		then: "The file uploading have been saved"
		1 * fileUploadingRepository.save(_) >> {
			FileUploading fileUploading = it[0];
			fileUploading.resourceId == RESOURCE_ID
			fileUploading.fileId == FILE_ID 
			fileUploading.uploadingMoment == UPLOADING_MOMENT
		}		
	}
}
