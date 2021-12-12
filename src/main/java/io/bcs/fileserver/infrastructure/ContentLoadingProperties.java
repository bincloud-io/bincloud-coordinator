package io.bcs.fileserver.infrastructure;

public interface ContentLoadingProperties {
  public int getBufferSize();

  public String getStorageName();

  public String getBaseDirectory();
}
