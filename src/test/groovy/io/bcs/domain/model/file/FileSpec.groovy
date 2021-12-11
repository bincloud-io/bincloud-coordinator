package io.bcs.domain.model.file


import static io.bcs.domain.model.file.FileStatus.DISPOSED
import static io.bcs.domain.model.file.FileStatus.DISTRIBUTING
import static io.bcs.domain.model.file.FileStatus.DRAFT

import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import io.bce.interaction.streaming.Destination
import io.bce.interaction.streaming.Source
import io.bce.interaction.streaming.binary.BinaryChunk
import io.bce.promises.Promise
import io.bce.promises.Promises
import io.bce.promises.WaitingPromise
import io.bce.promises.Promise.ErrorHandler
import io.bce.promises.Promise.ResponseHandler
import io.bcs.domain.model.Constants
import io.bcs.domain.model.file.File.CreateFile
import io.bcs.domain.model.file.FileContent.ContentPart
import io.bcs.domain.model.file.FileContent.ContentType
import io.bcs.domain.model.file.FileMetadata.Disposition
import io.bcs.domain.model.file.Lifecycle.FileUploadStatistic
import io.bcs.domain.model.file.states.ContentNotUploadedException
import io.bcs.domain.model.file.states.ContentUploadedException
import io.bcs.domain.model.file.states.FileDisposedException
import spock.lang.Specification

class FileSpec extends Specification {
  public static final String STORAGE_NAME = "storage.0001"
  public static final String STORAGE_FILE_NAME = "instance--${Thread.currentThread()}--${UUID.randomUUID()}"
  public static final String MEDIA_TYPE = "application/media-type-xxx"
  public static final String FILE_NAME = "file.txt"
  public static final Long DEFAULT_CONTENT_LENGTH = 0L
  public static final Long DISTRIBUTIONING_CONTENT_LENGTH = 100L

  private FileStorage fileStorage;
  private ContentReceiver contentDownloader;

  def setup() {
    this.fileStorage = Stub(FileStorage)
    this.contentDownloader = Mock(ContentReceiver)
  }

  def "Scenario: create file by default constructor"() {
    when: "The file is created by default constructor"
    File file = new File();
    ContentLocator fileContentLocator = file.getLocator();

    then: "The storage file name should be ${File.DEFAULT_STORAGE_FILE_NAME}"
    fileContentLocator.getStorageFileName() == File.DEFAULT_STORAGE_FILE_NAME

    and: "The storage name should be ${File.DEFAULT_STORAGE_NAME}"
    fileContentLocator.getStorageName() == File.DEFAULT_STORAGE_NAME

    and: "The file name should be ${File.DEFAULT_FILE_NAME}"
    file.getFileName() == File.DEFAULT_FILE_NAME

    and: "The file media type should be ${File.DEFAULT_MEDIA_TYPE}"
    file.getMediaType() == File.DEFAULT_MEDIA_TYPE

    and: "The file status should be ${FileStatus.DRAFT}"
    file.getStatus() == FileStatus.DRAFT

    and: "The file content length should be 0 bytes"
    file.getTotalLength() == 0L
  }

  def "Scenario: successfully create new file"() {
    File file
    given: """The file storage creates the file, located: {storageName: ${STORAGE_NAME}, storageFileName: ${STORAGE_FILE_NAME}} for ${MEDIA_TYPE}"""
    this.fileStorage.create(MEDIA_TYPE) >> contentLocator()

    and: "The file creation command with media type ${MEDIA_TYPE}"
    CreateFile command = createFileCommand(MEDIA_TYPE, FILE_NAME)

    and: "The file create response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    when: "The file is created"
    WaitingPromise.of(File.create(fileStorage, command)).then(responseHandler).await()

    then: "The response handler shoudl accept resolved file"
    1 * responseHandler.onResponse(_) >> {file = it[0]}
    ContentLocator fileContentLocator = file.getLocator();

    and: "The storage file name should be ${STORAGE_FILE_NAME}"
    fileContentLocator.getStorageFileName() == STORAGE_FILE_NAME

    and: "The storage name should be ${STORAGE_NAME}"
    fileContentLocator.getStorageName() == STORAGE_NAME

    and: "The file name should be ${FILE_NAME}"
    file.getFileName() == FILE_NAME

    and: "The file media type should be ${MEDIA_TYPE}"
    file.getMediaType() == MEDIA_TYPE

    and: "The file status should be ${FileStatus.DRAFT}"
    file.getStatus() == FileStatus.DRAFT

    and: "The file content length should be 0 bytes"
    file.getTotalLength() == 0L
  }

  def "Scenario: successfully create new file for missig media type and file name"() {
    File file
    given: """The file storage creates the file, located: {storageName: ${STORAGE_NAME}, storageFileName: ${STORAGE_FILE_NAME}} for ${File.DEFAULT_MEDIA_TYPE}"""
    this.fileStorage.create(File.DEFAULT_MEDIA_TYPE) >> contentLocator()

    and: "The file creation command with media type empty media type and file name"
    CreateFile command = createFileCommand(null, null)

    and: "The file create response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    when: "The file is created"
    WaitingPromise.of(File.create(fileStorage, command)).then(responseHandler).await()

    then: "The response handler shoudl accept resolved file"
    1 * responseHandler.onResponse(_) >> {file = it[0]}
    ContentLocator fileContentLocator = file.getLocator();

    and: "The storage file name should be ${STORAGE_FILE_NAME}"
    fileContentLocator.getStorageFileName() == STORAGE_FILE_NAME

    and: "The storage name should be ${STORAGE_NAME}"
    fileContentLocator.getStorageName() == STORAGE_NAME

    and: "The file name should be ${STORAGE_FILE_NAME}"
    file.getFileName() == STORAGE_FILE_NAME

    and: "The file media type should be ${File.DEFAULT_MEDIA_TYPE}"
    file.getMediaType() == File.DEFAULT_MEDIA_TYPE

    and: "The file status should be ${FileStatus.DRAFT}"
    file.getStatus() == FileStatus.DRAFT

    and: "The file content length should be 0 bytes"
    file.getTotalLength() == 0L
  }

  def "Scenario: create new file with file storage error"() {
    FileStorageException fileStorageException
    given: """The file storage creates the file, located: {storageName: ${STORAGE_NAME}, storageFileName: ${STORAGE_FILE_NAME}} for ${MEDIA_TYPE}"""
    this.fileStorage.create(MEDIA_TYPE) >> {throw new FileStorageException(new RuntimeException())}

    and: "The file creation command with media type ${MEDIA_TYPE}"
    CreateFile command = createFileCommand(MEDIA_TYPE, FILE_NAME)

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is created"
    WaitingPromise.of(File.create(fileStorage, command)).error(errorHandler).await()

    then: "The file storage error should be happened"
    1 * errorHandler.onError(_) >> {fileStorageException = it[0]}
    fileStorageException.getContextId() == Constants.CONTEXT
    fileStorageException.getErrorCode() == Constants.FILE_STORAGE_INCIDENT_ERROR
  }

  def "Scenario: dispose draft file"() {
    given: "The file in draft state"
    File file = createDraftFile()

    and: "The promise resolve response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    when: "The file is disposed"
    WaitingPromise.of(disposeFile(file)).then(responseHandler).await()

    then: "The response handler should be resolved"
    1 * responseHandler.onResponse(_)

    and: "The file status should be changed to the disposed"
    file.getStatus() == FileStatus.DISPOSED

    and: "The content length should be set to zero"
    file.getTotalLength() == 0L
  }


  def "Scenario: dispose distributioning file"() {
    given: "The file in distribution state"
    File file = createDistributionFile()

    and: "The promise resolve response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    when: "The file is disposed"
    WaitingPromise.of(disposeFile(file)).then(responseHandler).await()

    then: "The response handler should be resolved"
    1 * responseHandler.onResponse(_)

    and: "The file status should be changed to the disposed"
    file.getStatus() == FileStatus.DISPOSED

    and: "The content length should be set to zero"
    file.getTotalLength() == 0L
  }

  def "Scenario: dispose disposed file"() {
    FileDisposedException error
    given: "The file in disposed state"
    File file = createDisposedFile()

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is disposed"
    WaitingPromise.of(disposeFile(file)).error(errorHandler).await()

    then: "The file has already been exception should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorCode() == Constants.FILE_IS_DISPOSED_ERROR
  }

  def "Scenario: successfully upload file content to file in the draft state"() {
    FileUploadStatistic uploadStatistic

    given: "The file in draft state"
    File file = createDraftFile()

    and: "The content source"
    Source<BinaryChunk> source = Mock(Source)

    and: "The content uploader"
    Destination<BinaryChunk> destination = Mock(Destination)
    ContentUploader contentUploader = Mock(ContentUploader)
    contentUploader.upload(_, _) >> Promises.resolvedBy(createStatistic(file, DISTRIBUTIONING_CONTENT_LENGTH))

    and: "The promise response handler"
    ResponseHandler<FileUploadStatistic> responseHandler = Mock(ResponseHandler)

    when: "The file is uploaded"
    WaitingPromise.of(uploadFile(file, contentUploader)).then(responseHandler).await()
    ContentLocator fileLocator = file.getLocator()

    then: "The file upload should be completed successfully"
    1 * responseHandler.onResponse(_) >> {uploadStatistic = it[0]}
    ContentLocator statisticLocator = uploadStatistic.getLocator()
    statisticLocator.getStorageFileName() == fileLocator.getStorageFileName()
    statisticLocator.getStorageName() == fileLocator.getStorageName()
    uploadStatistic.getTotalLength() == DISTRIBUTIONING_CONTENT_LENGTH

    and: "The content length should be updated"
    file.getTotalLength() == DISTRIBUTIONING_CONTENT_LENGTH
    file.getStatus() == FileStatus.DISTRIBUTING
  }

  def "Scenario: upload file content with error to file in the draft state"() {
    FileStorageException fileStorageException

    given: "The file in draft state"
    File file = createDraftFile()

    and: "The content source"
    Source<BinaryChunk> source = Mock(Source)

    and: "The content uploader"
    ContentUploader contentUploader = Mock(ContentUploader)

    and: "The file storage throws file storage exception"
    fileStorage.getAccessOnWrite(_) >> {throw new FileStorageException(new RuntimeException())}

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is uploaded"
    WaitingPromise.of(uploadFile(file, contentUploader)).error(errorHandler).await()
    ContentLocator fileLocator = file.getLocator()

    then: "The file storage error should be happened"
    1 * errorHandler.onError(_) >> {fileStorageException = it[0]}
    fileStorageException.getContextId() == Constants.CONTEXT
    fileStorageException.getErrorCode() == Constants.FILE_STORAGE_INCIDENT_ERROR

    and: "The content length should not be updated"
    file.getTotalLength() == DEFAULT_CONTENT_LENGTH
  }

  def "Scenario: upload file content to file in the distributioning state"() {
    ContentUploadedException error
    given: "The file in distribution state"
    File file = createDistributionFile()

    and: "The content source"
    Source<BinaryChunk> source = Mock(Source)

    and: "The content uploader"
    ContentUploader contentUploader = Mock(ContentUploader)

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is uploaded"
    WaitingPromise.of(uploadFile(file, contentUploader)).error(errorHandler).await()

    then: "The file has already been exception should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorCode() == Constants.CONTENT_IS_UPLOADED_ERROR
  }

  def "Scenario: upload file content to file in the disposed state"() {
    FileDisposedException error
    given: "The file in disposed state"
    File file = createDisposedFile()

    and: "The content source"
    Source<BinaryChunk> source = Mock(Source)

    and: "The content uploader"
    ContentUploader contentUploader = Mock(ContentUploader)

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is uploaded"
    WaitingPromise.of(uploadFile(file, contentUploader)).error(errorHandler).await()

    then: "The file has already been exception should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorCode() == Constants.FILE_IS_DISPOSED_ERROR
  }

  def "Scenario: download file content from file in the draft state"() {
    ContentNotUploadedException error
    given: "The file in disposed state"
    File file = createDraftFile()

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is uploaded"
    WaitingPromise.of(file.downloadContent(fileStorage, contentDownloader, Collections.emptyList())).error(errorHandler).await()

    then: "The file has already been exception should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorCode() == Constants.CONTENT_IS_NOT_UPLOADED_ERROR
  }

  def "Scenario: download file content from file in the disposed state"() {
    FileDisposedException error
    given: "The file in disposed state"
    File file = createDisposedFile()

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is uploaded"
    WaitingPromise.of(file.downloadContent(fileStorage, contentDownloader, Collections.emptyList())).error(errorHandler).await()

    then: "The file has already been exception should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorCode() == Constants.FILE_IS_DISPOSED_ERROR
  }

  def "Scenario: download full file content"() {
    FileContent fileContent
    given: "The file in distribution state"
    File file = createDistributionFile()

    and: "The promise response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    and: "The content parts should not be passed"
    Collection<ContentFragment> fragments = Collections.emptyList()

    and: "The file storage should return binary source for download"
    Source<BinaryChunk> source = Stub(Source)
    fileStorage.getAccessOnRead(_, _) >> source

    when: "The file is downloaded"
    WaitingPromise.of(file.downloadContent(fileStorage, contentDownloader, fragments)).then(responseHandler).await()

    then: "The operation should be completed without error"
    1 * responseHandler.onResponse(_)

    and: "The full content download operation should be passed"
    1 * contentDownloader.receiveFullContent(_) >> {
      fileContent = it[0]
      return Promises.resolvedBy(null)
    }

    and: "The file content type should be ${ContentType.FULL}"
    fileContent.getType() == ContentType.FULL

    and: "The file locator from file should be equal to file locator from the file content"
    file.getLocator().getStorageName() == fileContent.getLocator().getStorageName()
    file.getLocator().getStorageFileName() == fileContent.getLocator().getStorageFileName()

    and: "The content part should represent whole file"
    fileContent.getParts().size() == 1
    ContentPart contentPart = fileContent.getParts()[0]
    contentPart.getContentFragment().getOffset() == 0L
    contentPart.getContentFragment().getLength() == file.getTotalLength()
    contentPart.getContentSource() == source
  }

  def "Scenario: download file content for wrong ranges"() {
    UnsatisfiableRangeFormatException error
    given: "The file in distribution state"
    File file = createDistributionFile()

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    and: "The content parts should not be passed"
    Collection<ContentFragment> fragments = [
      createRange(10L, 30L),
      createRange(-10L, 100L)
    ]

    and: "The file storage should return binary source for download"
    Source<BinaryChunk> source = Stub(Source)
    fileStorage.getAccessOnRead(_, _) >> source

    when: "The file is downloaded"
    WaitingPromise.of(file.downloadContent(fileStorage, contentDownloader, fragments)).error(errorHandler).await()

    then: "The unsatisfiable range format error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
    error.getErrorCode() == Constants.UNSATISFIABLE_RANGES_FORMAT_ERROR
  }

  def "Scenario: download partial file content"() {
    FileContent fileContent
    given: "The file in distribution state"
    File file = createDistributionFile()

    and: "The promise response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    and: "The content parts should not be passed"
    Collection<ContentFragment> fragments = Arrays.asList(createRange(10L, 29L))

    and: "The file storage should return binary source for download"
    Source<BinaryChunk> source = Stub(Source)
    fileStorage.getAccessOnRead(_, _) >> source

    when: "The file is downloaded"
    WaitingPromise.of(file.downloadContent(fileStorage, contentDownloader, fragments)).then(responseHandler).await()

    then: "The operation should be completed without error"
    1 * responseHandler.onResponse(_)

    and: "The full content download operation should be passed"
    1 * contentDownloader.receiveContentRange(_) >> {
      fileContent = it[0]
      return Promises.resolvedBy(null)
    }

    and: "The file content type should be ${ContentType.RANGE}"
    fileContent.getType() == ContentType.RANGE

    and: "The file locator from file should be equal to file locator from the file content"
    file.getLocator().getStorageName() == fileContent.getLocator().getStorageName()
    file.getLocator().getStorageFileName() == fileContent.getLocator().getStorageFileName()

    and: "The content part should represent single specified fragment"
    fileContent.getParts().size() == 1
    ContentPart contentPart = fileContent.getParts()[0]
    contentPart.getContentFragment().getOffset() == 10L
    contentPart.getContentFragment().getLength() == 20L
    contentPart.getContentSource() == source
  }

  def "Scenario: download multirange file content"() {
    FileContent fileContent
    given: "The file in distribution state"
    File file = createDistributionFile()

    and: "The promise response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    and: "The content parts should not be passed"
    Collection<ContentFragment> fragments = Arrays.asList(createRange(10L, 29L), createRange(30L, 49L))

    and: "The file storage should return binary source for download"
    Source<BinaryChunk> firstSource = Stub(Source)
    Source<BinaryChunk> secondSource = Stub(Source)
    fileStorage.getAccessOnRead(_, _) >> firstSource >> secondSource

    when: "The file is downloaded"
    WaitingPromise.of(file.downloadContent(fileStorage, contentDownloader, fragments)).then(responseHandler).await()

    then: "The operation should be completed without error"
    1 * responseHandler.onResponse(_)

    and: "The full content download operation should be passed"
    1 * contentDownloader.receiveContentRanges(_) >> {
      fileContent = it[0]
      return Promises.resolvedBy(null)
    }

    and: "The file content type should be ${ContentType.MULTIRANGE}"
    fileContent.getType() == ContentType.MULTIRANGE

    and: "The file locator from file should be equal to file locator from the file content"
    file.getLocator().getStorageName() == fileContent.getLocator().getStorageName()
    file.getLocator().getStorageFileName() == fileContent.getLocator().getStorageFileName()

    and: "The two content parts should be contained into request"
    fileContent.getParts().size() == 2

    and: "The first part should represent first specified fragment"
    ContentPart firstPart = fileContent.getParts()[0]
    firstPart.getContentFragment().getOffset() == 10L
    firstPart.getContentFragment().getLength() == 20L
    firstPart.getContentSource() == firstSource

    and: "The second part should represent first specified fragment"
    ContentPart secondPart = fileContent.getParts()[1]
    secondPart.getContentFragment().getOffset() == 30L
    secondPart.getContentFragment().getLength() == 20L
    secondPart.getContentSource() == secondSource
  }

  private Range createRange(Long start, Long end) {
    return new Range() {
          @Override
          public Optional<Long> getStart() {
            return Optional.of(start);
          }

          @Override
          public Optional<Long> getEnd() {
            return Optional.of(end);
          }
        }
  }

  private File createDraftFile() {
    return createFile(DRAFT, 0L)
  }

  private File createDistributionFile() {
    return createFile(DISTRIBUTING, DISTRIBUTIONING_CONTENT_LENGTH)
  }

  private File createDisposedFile() {
    return createFile(DISPOSED, 0L)
  }

  private Promise<FileUploadStatistic> uploadFile(File file, ContentUploader uploader) {
    return file.getLifecycle(fileStorage).upload(uploader).execute()
  }

  private Promise<Void> disposeFile(File file) {
    return file.getLifecycle(fileStorage).dispose().execute()
  }

  private FileUploadStatistic createStatistic(File file, Long contentSize) {
    return new FileUploadStatistic() {

          @Override
          public ContentLocator getLocator() {
            return file.getLocator();
          }

          @Override
          public Long getTotalLength() {
            return contentSize;
          }
        }
  }

  private File createFile(FileStatus status, Long contentLength) {
    return File.builder()
        .storageName(STORAGE_NAME)
        .storageFileName(STORAGE_FILE_NAME)
        .status(status)
        .mediaType(MEDIA_TYPE)
        .fileName(FILE_NAME)
        .totalLength(contentLength)
        .build()
  }

  private CreateFile createFileCommand(String mediaType, String fileName) {
    CreateFile command = Stub(CreateFile)
    command.getMediaType() >> Optional.ofNullable(mediaType)
    command.getFileName() >> Optional.ofNullable(fileName)
    return command
  }

  private ContentLocator contentLocator() {
    ContentLocator contentLocator = Stub(ContentLocator)
    contentLocator.getStorageName() >> STORAGE_NAME
    contentLocator.getStorageFileName() >> STORAGE_FILE_NAME
    return contentLocator
  }
}
