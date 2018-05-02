package it.greenvulcano.gvesb.api.dto;

import it.greenvulcano.gvesb.iam.domain.Role;

public class RoleDTO extends Role {
	
	private Integer id;
	private String name,description;
	
	@Override
	public Integer getId() {
		
		return id;
	}

	@Override
	public String getName() {		
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	@Override
	public String getDescription() {		
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;

	}

}
