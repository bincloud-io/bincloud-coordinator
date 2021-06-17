package io.bincloud.resources.port.adapter;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServerContextProvider {
	public Integer getIOBufferSize() {
		return 4096;
	}
	
	public String getInstanceId() {
		return "global";
	}
	
	public String getRootFilesFolderPath() {
		return String.format("/srv/bincloud/wildfly/filestore/%s", getInstanceId());
	}
	
	public String getRootURL() {
		return "http://localhost:38080/";
	}
}