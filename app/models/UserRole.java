package models;

import be.objectify.deadbolt.core.models.Role;

public class UserRole implements Role {

	public Long id;
	public String name;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
