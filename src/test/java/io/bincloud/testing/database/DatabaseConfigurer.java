package io.bincloud.testing.database;

public interface DatabaseConfigurer {
	public void setup(String configLocation);
	public void tearDown();
}
