package it.greenvulcano.gvesb.api.model;

import java.util.Objects;

public class Operation {

	private final String name;
	private final boolean enabled;
	
	public Operation(String name, boolean enabled) {	
		this.name = Objects.requireNonNull(name);
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public boolean isEnabled() {
		return enabled;
	}	
	
}
