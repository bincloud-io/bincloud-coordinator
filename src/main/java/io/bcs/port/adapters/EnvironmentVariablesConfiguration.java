package io.bcs.port.adapters;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EnvironmentVariablesConfiguration implements ContentLoadingProperties, ActorSystemProperties {
    private static final String BUFFER_SIZE_VAR = "BC_IO_BUFFER_SIZE";
    private static final String CONTENT_FOLDER_VAR = "BC_CONTENT_FOLDER";
    private static final String INSTANCE_ID_VAR = "BC_INSTANCE";

    @Override
    public int getBufferSize() {
        return Integer.valueOf(System.getenv(BUFFER_SIZE_VAR));
    }

    @Override
    public String getStorageName() {
        return "LOCAL";
    }

    @Override
    public String getBaseDirectory() {
        return System.getenv(CONTENT_FOLDER_VAR);
    }

    @Override
    public String getInstanceId() {
        return System.getenv(INSTANCE_ID_VAR);
    }
}
