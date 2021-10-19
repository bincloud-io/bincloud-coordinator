package io.bcs.storage.port.adapter;

public interface ServerContextProvider {
	public int getIOBufferSize();
	public String getRootFilesFolderPath();
	public String getInstanceId();
}
