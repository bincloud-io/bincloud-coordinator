package io.bcs.fileserver.infrastructure.api.internal;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bcs.common.errors.SoapFault;
import io.bcs.fileserver.api.services.internal.CreateFileFault;
import io.bcs.fileserver.api.services.internal.DeleteFileFault;
import io.bcs.fileserver.api.services.internal.GetContentReadUrlFault;
import io.bcs.fileserver.api.services.internal.GetContentWriteUrlFault;
import io.bcs.fileserver.api.services.internal.WSRemoteStorage;
import io.bcs.fileserver.api.types.internal.ContentDirectAccessUrlRsType;
import io.bcs.fileserver.api.types.internal.CreateFileRqType;
import io.bcs.fileserver.api.types.internal.CreateFileRsType;
import io.bcs.fileserver.api.types.internal.DeleteFileRqType;
import io.bcs.fileserver.api.types.internal.GetDirectContentReadUrlRqType;
import io.bcs.fileserver.api.types.internal.GetDirectContentWriteUrlRqType;
import io.bcs.fileserver.domain.Constants;
import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import io.bcs.fileserver.types.ContentFragmentType;
import io.bcs.fileserver.types.ContentLocatorType;
import io.bcs.global.types.ServiceResponseType;
import javax.inject.Inject;
import javax.jws.WebService;

/**
 * This class implements web-service managing remote direct access to the file content.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@WebService(
    serviceName = "WSContentAccess",
    endpointInterface = "io.bcs.fileserver.api.services.internal.WSContentAccess")
public class WsContentAccessService implements WSRemoteStorage {
  private static final String READ_URL_PATH_FORMAT =
      "%s/private/access/direct/content?storageName=%s&storageFileName=%s&offset=%s&length=%s";
  private static final String WRITE_URL_PATH_FORMAT =
      "%s/private/access/direct/content?storageName=%s&storageFileName=%s";

  ApplicationLogger logger = Loggers.applicationLogger(WsContentAccessService.class);

  @Inject
  private FileServerConfigurationProperties configurationProperties;

  @Override
  public CreateFileRsType createFile(CreateFileRqType parameters) throws CreateFileFault {
    return null;
  }

  @Override
  public ContentDirectAccessUrlRsType getContentReadUrl(GetDirectContentReadUrlRqType request)
      throws GetContentReadUrlFault {
    try {
      return new WsContentReadUrlRs(request);
    } catch (Exception error) {
      SoapFault faultInfo = SoapFault.createFor(error);
      throw new GetContentReadUrlFault(faultInfo.getMessage(), faultInfo);
    }
  }

  @Override
  public ContentDirectAccessUrlRsType getContentWriteUrl(GetDirectContentWriteUrlRqType parameters)
      throws GetContentWriteUrlFault {
    try {
      return new WsContentWriteUrlRs(parameters);
    } catch (Exception error) {
      SoapFault faultInfo = SoapFault.createFor(error);
      throw new GetContentWriteUrlFault(faultInfo.getMessage(), faultInfo);
    }
  }

  @Override
  public ServiceResponseType deleteFile(DeleteFileRqType parameters) throws DeleteFileFault {
    return null;
  }

  private String getContextRoot() {
    return configurationProperties.getPrivateBaseUrlAddress();
  }

  private class WsContentReadUrlRs extends ContentDirectAccessUrlRsType {
    WsContentReadUrlRs(GetDirectContentReadUrlRqType request) {
      super();
      setBoundedContext(Constants.CONTEXT.toString());
      ContentLocatorType locator = request.getLocator();
      ContentFragmentType fragment = request.getFragment();
      setContentAccessUrl(
          String.format(READ_URL_PATH_FORMAT, getContextRoot(), locator.getStorageName(),
              locator.getStorageFileName(), fragment.getOffset(), fragment.getLength()));
    }
  }

  private class WsContentWriteUrlRs extends ContentDirectAccessUrlRsType {

    public WsContentWriteUrlRs(GetDirectContentWriteUrlRqType request) {
      super();
      setBoundedContext(Constants.CONTEXT.toString());
      ContentLocatorType locator = request.getContentLocator();
      setContentAccessUrl(String.format(WRITE_URL_PATH_FORMAT, locator.getStorageName(),
          locator.getStorageFileName()));
    }
  }
}
