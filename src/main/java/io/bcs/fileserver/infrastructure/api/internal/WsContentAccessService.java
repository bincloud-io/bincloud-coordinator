package io.bcs.fileserver.infrastructure.api.internal;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bcs.common.errors.SoapFault;
import io.bcs.fileserver.api.services.internal.GetContentReadUrlFault;
import io.bcs.fileserver.api.services.internal.GetContentWriteUrlFault;
import io.bcs.fileserver.api.services.internal.WSContentAccess;
import io.bcs.fileserver.api.types.internal.GetDirectContentReadUrlRqType;
import io.bcs.fileserver.api.types.internal.GetDirectContentReadUrlRsType;
import io.bcs.fileserver.api.types.internal.GetDirectContentWriteUrlRqType;
import io.bcs.fileserver.api.types.internal.GetDirectContentWriteUrlRsType;
import io.bcs.fileserver.domain.Constants;
import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import javax.inject.Inject;
import javax.jws.WebService;
import lombok.RequiredArgsConstructor;

/**
 * This class implements web-service managing remote direct access to the file content.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@WebService(
    serviceName = "WSContentAccess",
    endpointInterface = "io.bcs.fileserver.api.services.internal.WSContentAccess")
public class WsContentAccessService implements WSContentAccess {
  private static final String READ_URL_PATH_FORMAT =
      "%s/private/access/direct/content?storageName=%s&storageFileName=%s&offset=%s&length=%s";
  private static final String WRITE_URL_PATH_FORMAT =
      "%s/private/access/direct/content?storageName=%s&storageFileName=%s";

  ApplicationLogger logger = Loggers.applicationLogger(WsContentAccessService.class);

  @Inject
  private FileServerConfigurationProperties configurationProperties;

  @Override
  public GetDirectContentReadUrlRsType getContentReadUrl(GetDirectContentReadUrlRqType request)
      throws GetContentReadUrlFault {
    try {
      GetDirectContentReadUrlRsType response = new GetDirectContentReadUrlRsType();
      ReadContentUrlCreator urlCreator = new ReadContentUrlCreator(request);
      response.setBoundedContext(Constants.CONTEXT.toString());
      response.setContentRef(urlCreator.createContentReadRef());
      return response;
    } catch (Exception error) {
      SoapFault faultInfo = SoapFault.createFor(error);
      throw new GetContentReadUrlFault(faultInfo.getMessage(), faultInfo);
    }
  }

  @Override
  public GetDirectContentWriteUrlRsType getContentWriteUrl(
      GetDirectContentWriteUrlRqType parameters) throws GetContentWriteUrlFault {
    try {
      GetDirectContentWriteUrlRsType response = new GetDirectContentWriteUrlRsType();
      WriteContentUrlCreator urlCreator = new WriteContentUrlCreator(parameters);
      response.setBoundedContext(Constants.CONTEXT.toString());
      response.setContentRef(urlCreator.createContentWriteRef());
      return response;
    } catch (Exception error) {
      SoapFault faultInfo = SoapFault.createFor(error);
      throw new GetContentWriteUrlFault(faultInfo.getMessage(), faultInfo);
    }
  }

  @RequiredArgsConstructor
  private class ReadContentUrlCreator {
    private final GetDirectContentReadUrlRqType request;

    public String createContentReadRef() {
      return String.format(READ_URL_PATH_FORMAT, getContextRoot(), getStorageName(),
          getStorageFileName(), getOffset(), getLength());
    }

    private String getContextRoot() {
      return configurationProperties.getPrivateBaseUrlAddress();
    }

    private String getStorageName() {
      return request.getLocator().getStorageName();
    }

    private String getStorageFileName() {
      return request.getLocator().getStorageFileName();
    }

    private Long getOffset() {
      return request.getFragment().getOffset();
    }

    private Long getLength() {
      return request.getFragment().getLength();
    }
  }

  @RequiredArgsConstructor
  private class WriteContentUrlCreator {
    private final GetDirectContentWriteUrlRqType request;

    public String createContentWriteRef() {
      return String.format(WRITE_URL_PATH_FORMAT, getContextRoot(), getStorageName(),
          getStorageFileName());
    }

    private String getContextRoot() {
      return configurationProperties.getPrivateBaseUrlAddress();
    }

    private String getStorageName() {
      return request.getLocator().getStorageName();
    }

    private String getStorageFileName() {
      return request.getLocator().getStorageFileName();
    }
  }
}
