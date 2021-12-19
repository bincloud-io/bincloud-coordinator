package io.bcs.fileserver.infrastructure.controllers.soap;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bcs.common.errors.SoapFault;
import io.bcs.fileserver.domain.Constants;
import io.bcs.fileserver.domain.model.file.File.CreateFile;
import io.bcs.fileserver.domain.model.file.FileManagement;
import io.bcs.fileserver.domain.services.acl.SafeCreateFileCommand;
import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import io.bcs.fileserver.soap.endpoints.files.CreateFileFault;
import io.bcs.fileserver.soap.endpoints.files.DisposeFileFault;
import io.bcs.fileserver.soap.endpoints.files.WSFileService;
import io.bcs.fileserver.soap.types.file.storage.CreateFileRqType;
import io.bcs.fileserver.soap.types.file.storage.CreateFileRsType;
import io.bcs.fileserver.soap.types.file.storage.DisposeFileRqType;
import io.bcs.fileserver.soap.types.file.storage.DisposeFileRsType;
import java.util.Optional;
import javax.inject.Inject;
import javax.jws.WebService;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * This class implements the file management service SOAP controller.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@WebService(
    serviceName = "WSFileService",
    endpointInterface = "io.bcs.port.adapter.file.endpoint.WSFileService")
public class SoapFileServiceEndpoint implements WSFileService {
  private static final ApplicationLogger log =
      Loggers.applicationLogger(SoapFileServiceEndpoint.class);

  @Inject
  private FileManagement fileService;

  @Inject
  private FileServerConfigurationProperties fileserverConfig;

  @Override
  public CreateFileRsType createFile(CreateFileRqType parameters) throws CreateFileFault {
    try {

      CreateFileRsType response = new CreateFileRsType();
      CreateFile createCommand = new WsCreateFileCommand(parameters);
      Promise<String> result = fileService.createFile(createCommand);
      response.setBoundedContext(Constants.CONTEXT.toString());
      response.setFileReference(
          getFileReference(result.get(fileserverConfig.getSyncOperationTimeout())));
      return response;
    } catch (Exception error) {
      log.error(error);
      SoapFault faultInfo = SoapFault.createFor(error);
      throw new CreateFileFault(faultInfo.getMessage(), faultInfo, error);
    }
  }

  @Override
  public DisposeFileRsType disposeFile(DisposeFileRqType parameters) throws DisposeFileFault {
    try {
      DisposeFileRsType response = new DisposeFileRsType();
      fileService.disposeFile(parameters.getStorageFileName())
          .get(fileserverConfig.getSyncOperationTimeout());
      response.setBoundedContext(Constants.CONTEXT.toString());
      return response;
    } catch (Exception error) {
      log.error(error);
      SoapFault faultInfo = SoapFault.createFor(error);
      throw new DisposeFileFault(faultInfo.getMessage(), faultInfo, error);
    }
  }

  private String getFileReference(String fileStorageName) {
    return String.format("%s/file-content?fileStorageName=%s", fileserverConfig.getBaseUrlAddress(),
        fileStorageName);
  }

  @ToString
  @RequiredArgsConstructor
  private static class WsCreateFileCommand extends SafeCreateFileCommand {
    private final CreateFileRqType request;

    @Override
    public Optional<String> getMediaType() {
      return Optional.ofNullable(request.getMediaType());
    }

    @Override
    public Optional<String> getFileName() {
      return Optional.ofNullable(request.getFileName());
    }
  }

}
