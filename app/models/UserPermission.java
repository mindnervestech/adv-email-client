package models;

import be.objectify.deadbolt.core.models.Permission;

public class UserPermission implements Permission {

	public Long id;
	public String name;
	public String url;
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public String getValue() {
		return name;
	}
	
}
