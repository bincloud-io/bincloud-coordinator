package io.bcs.storage.port.adapter.config;

import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.bcs.storage.port.adapter.ServerContextProvider;

@ApplicationScoped
public class ServerContextConfig {
	@Produces
	public ServerContextProvider contextProvider() {
		return new PropertiesServerContextProvider(new Properties());
	}
}
