package io.bincloud.storage.port.adapters.file.filesystem

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith

import io.bincloud.common.domain.model.error.ApplicationException
import io.bincloud.common.domain.model.error.MustNeverBeHappenedError
import io.bincloud.common.domain.model.io.InputOutputException
import io.bincloud.common.domain.model.io.transfer.CompletionCallback
import io.bincloud.common.domain.model.io.transfer.DestinationPoint
import io.bincloud.common.domain.model.io.transfer.SourcePoint
import io.bincloud.common.domain.model.logging.Loggers
import io.bincloud.common.domain.model.message.MessageTemplate
import io.bincloud.common.port.adapters.io.transfer.destinations.StreamDestination
import io.bincloud.common.port.adapters.io.transfer.sources.StreamSource
import io.bincloud.common.port.adapters.io.transfer.transmitter.DirectTransferingScheduler
import io.bincloud.storage.domain.model.file.File
import io.bincloud.storage.domain.model.file.FilesystemAccessor
import io.bincloud.storage.port.adapter.file.filesystem.BlockedFileSystemAccessor
import io.bincloud.storage.port.adapter.file.filesystem.FilesystemStreamDestination
import io.bincloud.storage.port.adapter.file.filesystem.FilesystemStreamSource
import io.bincloud.testing.archive.ArchiveBuilder
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class BlockedFileSystemAccessorITSpec extends Specification {
	private static final String DIRECTORY_NAME = UUID.randomUUID().toString();

	@Deployment
	public static Archive "create deployment"() {
		return ArchiveBuilder.jar("blocked-filesystem-accessor-spec.jar")
				.appendPackageNonRecursively(File.getPackage().getName())
				.appendPackagesRecursively(ApplicationException.getPackage().getName())
				.appendPackagesRecursively(InputOutputException.getPackage().getName())
				.appendPackagesRecursively(Loggers.getPackage().getName())
				.appendPackagesRecursively(MessageTemplate.getPackage().getName())
				.appendPackagesRecursively(DirectTransferingScheduler.getPackage().getName())
				.appendPackagesRecursively(StreamSource.getPackage().getName())
				.appendPackagesRecursively(StreamDestination.getPackage().getName())
				.appendClasses(BlockedFileSystemAccessor, FilesystemStreamSource, FilesystemStreamDestination)
				.build()
	}
	
	private FilesystemAccessor filesystemAccessor;
	
	def setup() {
		java.io.File file = createRootDirectory();
		this.filesystemAccessor = new BlockedFileSystemAccessor(file.getPath(), 1000)
	}
	
	def cleanup() {
		deleteRootDirectory()
	}
	
	def "Scenario: file successfully created"() {
		when: "The not existing file has been created"
		filesystemAccessor.createFile("file.txt")
		
		then: "Any exception shouldn't be thrown"
		noExceptionThrown()
		
		and: "Created file should be existed on the filesystem"
		new java.io.File(openRootDirectory(), "file.txt").exists() == true
	}
	
	def "Scenario: create existing file"() {
		given: "The existing file"
		createFile("file.txt")
		
		when: "The file creation has been requested"
		this.filesystemAccessor.createFile("file.txt")
		
		then: "The file already exists should be thrown"
		thrown(MustNeverBeHappenedError)
	}
	
	def "Scenario: get file size for existing file"() {
		given: "The existing file"
		createFile("file.txt")
		
		expect: "The file size should be zero"
		this.filesystemAccessor.getFileSize("file.txt") == 0L
	}
	
	def "Scenario: get file size for not existing file"() {
		when: "The file size has been requested for not existing file"
		this.filesystemAccessor.getFileSize("file.txt")
		
		then: "The file not exists exception should be thrown"
		thrown(MustNeverBeHappenedError)
	}
	
	def "Scenario: transfer data between files using direct transmission"() {
		CompletionCallback callback = Mock(CompletionCallback)
		given: "Non empty source file"
		createFile("source-file.txt")
		writeStringToFile("source-file.txt", "SOME TEXT!!!SOME TEXT!!!SOME TEXT!!!SOME TEXT!!!")
		
		and: "Empty destination file"		
		createFile("destination-file.txt")
		
		when: "The transmission has been completed"
		DirectTransferingScheduler scheduler = new DirectTransferingScheduler()
		Long fileSize = this.filesystemAccessor.getFileSize("source-file.txt") - 15
		SourcePoint sourcePoint = this.filesystemAccessor.getAccessOnRead("source-file.txt", 0, fileSize)
		DestinationPoint destinationPoint = this.filesystemAccessor.getAccessOnWrite("destination-file.txt")
		scheduler.schedule(sourcePoint, destinationPoint, callback).start()
		
		then: "These file must contain the same data"
		1 * callback.onSuccess();
		readStringFromFile("destination-file.txt") == "SOME TEXT!!!SOME TEXT!!!SOME TEXT"
	}
	
	private java.io.File openRootDirectory() {
		return new java.io.File(DIRECTORY_NAME);
	}
	
	private java.io.File createRootDirectory() {
		java.io.File dir = openRootDirectory()
		dir.mkdir()
		return dir
	}
	
	private void deleteRootDirectory() {
		openRootDirectory().deleteDir()
	}
	
	private java.io.File openFile(fileName) {
		return new java.io.File(openRootDirectory(), fileName)
	}
	private void createFile(fileName) {
		openFile(fileName).createNewFile()
	}
	
	private void writeStringToFile(String fileName, String text) {
		new FileOutputStream(openFile(fileName)).write(text.getBytes())
	}
	
	private String readStringFromFile(filename) {
		return new String(openFile(filename).readBytes())
	}
}
