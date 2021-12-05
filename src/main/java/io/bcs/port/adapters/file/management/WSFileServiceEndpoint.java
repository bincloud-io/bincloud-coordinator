package io.bcs.port.adapters.file.management;

import java.util.Optional;

import javax.inject.Inject;
import javax.jws.WebService;

import io.bce.promises.Promise;
import io.bcs.application.FileService;
import io.bcs.application.acl.SafeCreateFileCommand;
import io.bcs.domain.model.Constants;
import io.bcs.domain.model.file.File.CreateFile;
import io.bcs.port.adapter.file.CreateFileRqType;
import io.bcs.port.adapter.file.CreateFileRsType;
import io.bcs.port.adapter.file.DisposeFileRqType;
import io.bcs.port.adapter.file.DisposeFileRsType;
import io.bcs.port.adapter.file.endpoint.CreateFileFault;
import io.bcs.port.adapter.file.endpoint.DisposeFileFault;
import io.bcs.port.adapter.file.endpoint.WSFileService;
import io.bcs.port.adapters.FilesManagementProperties;
import io.bcs.port.adapters.TimeoutProperties;
import io.bcs.port.adapters.common.WSFault;
import lombok.RequiredArgsConstructor;

@WebService(
        serviceName = "WSFileService", 
        endpointInterface = "io.bcs.port.adapter.file.endpoint.WSFileService")
public class WSFileServiceEndpoint implements WSFileService {
    @Inject
    private FileService fileService;
    
    @Inject
    private TimeoutProperties timeoutProperties;
    
    @Inject
    private FilesManagementProperties filesManagementProperties;

    @Override
    public CreateFileRsType createFile(CreateFileRqType parameters) throws CreateFileFault {
        try {
            CreateFileRsType response = new CreateFileRsType();
            CreateFile createCommand = new WSCreateFileCommand(parameters);
            Promise<String> result = fileService.createFile().execute(createCommand);
            response.setBoundedContext(Constants.CONTEXT.toString());
            response.setFileReference(getFileReference(result.get(timeoutProperties.getSyncOperationTimeout())));
            return response;
        } catch (Exception error) {
            WSFault faultInfo = WSFault.createFor(error);
            throw new CreateFileFault(faultInfo.getMessage(), faultInfo, error);
        }
    }

    @Override
    public DisposeFileRsType disposeFile(DisposeFileRqType parameters) throws DisposeFileFault {
        try {
            DisposeFileRsType response = new DisposeFileRsType();
            fileService.disposeFile().execute(parameters.getStorageFileName())
                    .get(timeoutProperties.getSyncOperationTimeout());
            response.setBoundedContext(Constants.CONTEXT.toString());
            return response;
        } catch (Exception error) {
            WSFault faultInfo = WSFault.createFor(error);
            throw new DisposeFileFault(faultInfo.getMessage(), faultInfo, error);
        }
    }

    private String getFileReference(String fileStorageName) {
        return String.format("%s/file-content?fileStorageName=%s", filesManagementProperties.getBaseUrlAddress(),
                fileStorageName);
    }

    @RequiredArgsConstructor
    private static class WSCreateFileCommand extends SafeCreateFileCommand {
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
