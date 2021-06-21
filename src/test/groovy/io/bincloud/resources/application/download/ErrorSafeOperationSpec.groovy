package io.bincloud.resources.application.download

import io.bincloud.resources.application.download.operations.ErrorSafeDownloadOperation
import io.bincloud.resources.domain.model.contracts.FileDownloader.DownloadCallback
import spock.lang.Specification

class ErrorSafeOperationSpec extends Specification {
	def "Scenario: success download case"() {
		DownloadOperation unsafeOperation = Mock(DownloadOperation)
		given: "The unsafe download operation completes successfully"	
		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)
		and: "The error safe operation wraps unsafe operation"
		DownloadOperation safeOperation = new ErrorSafeDownloadOperation({-> unsafeOperation}, downloadCallback) 

		when: "The safe operation calls"		
		safeOperation.downloadFile()
		
		then: "The unsafe operation should be called"
		1 * unsafeOperation.downloadFile()
	}

	def "Scenario: thrown error case"() {
		RuntimeException error = new RuntimeException("Somthing went wrong")
		DownloadOperation unsafeOperation = Mock(DownloadOperation)
		given: "The unsafe download operation completes with error"
		unsafeOperation.downloadFile() >> {throw error}
		
		and: "The download callback"
		DownloadCallback downloadCallback = Mock(DownloadCallback)
		
		and: "The error safe operation wraps unsafe operation"
		DownloadOperation safeOperation = new ErrorSafeDownloadOperation({-> unsafeOperation}, downloadCallback)

		when: "The safe operation calls"
		safeOperation.downloadFile()
		
		then: "The download callback should be called with thrown error"
		1 * downloadCallback.onError(error)
	}
}
