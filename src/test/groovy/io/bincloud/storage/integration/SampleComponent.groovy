package io.bincloud.storage.integration

import javax.enterprise.context.Dependent

@Dependent
class SampleComponent {
	public String getGreeting() {
		return "Hello"
	}
}
