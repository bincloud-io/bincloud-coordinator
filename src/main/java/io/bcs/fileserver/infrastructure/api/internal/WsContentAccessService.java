package io.bcs.fileserver.infrastructure.api.internal;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bcs.common.errors.SoapFault;
import io.bcs.fileserver.api.services.internal.GetDirectContentUrlFault;
import io.bcs.fileserver.api.services.internal.WSContentAccess;
import io.bcs.fileserver.api.types.internal.GetDirectContentUrlRqType;
import io.bcs.fileserver.api.types.internal.GetDirectContentUrlRsType;
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
  private static final String URL_PATH_FORMAT =
      "%s/private/access/direct/content?storageName=%s&storageFileName=%s&offset=%s&length=%s";
  ApplicationLogger logger = Loggers.applicationLogger(WsContentAccessService.class);

  @Inject
  private FileServerConfigurationProperties configurationProperties;

  @Override
  public GetDirectContentUrlRsType getDirectContentUrl(GetDirectContentUrlRqType request)
      throws GetDirectContentUrlFault {
    try {
      GetDirectContentUrlRsType response = new GetDirectContentUrlRsType();
      DirectContentUrlCreator urlCreator = new DirectContentUrlCreator(request);
      response.setBoundedContext(Constants.CONTEXT.toString());
      response.setContentRef(urlCreator.createContentRef());
      return response;
    } catch (Exception error) {
      SoapFault faultInfo = SoapFault.createFor(error);
      throw new GetDirectContentUrlFault(faultInfo.getMessage(), faultInfo);
    }
  }

  @RequiredArgsConstructor
  private class DirectContentUrlCreator {
    private final GetDirectContentUrlRqType request;

    public String createContentRef() {
      return String.format(URL_PATH_FORMAT, getContextRoot(), getStorageFileName(), getOffset(),
          getLength());
    }

    private String getContextRoot() {
      return configurationProperties.getPrivateBaseUrlAddress();
    }

    private String getStorageFileName() {
      return request.getStorageFileName();
    }

    private Long getOffset() {
      return request.getFragment().getOffset();
    }

    private Long getLength() {
      return request.getFragment().getLength();
    }
  }
}
