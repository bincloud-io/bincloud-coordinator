package io.bcs.files.port.adapter;

public interface ServerContextProvider {
	public int getIOBufferSize();
	public String getRootFilesFolderPath();
	public String getInstanceId();
}
