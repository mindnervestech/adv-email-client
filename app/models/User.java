package models;

import java.util.List;


import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;


public class User implements Subject{

	public Long id;
	public String username;
	public String password;
	public Long companyId;
	public List<UserRole> userRoles;
	public List<UserPermission> userPermissions;
	
	public User(String username,String password) {
		this.username = username;
		this.password = password;
	}
	
	@Override
	public String getIdentifier() {
		return username;
	}
	@Override
	public List<? extends Permission> getPermissions() {
		return userPermissions;
	}
	@Override
	public List<? extends Role> getRoles() {
		return userRoles;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<UserRole> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(List<UserRole> userRoles) {
		this.userRoles = userRoles;
	}

	public List<UserPermission> getUserPermissions() {
		return this.userPermissions;
	}

	public void setUserPermissions(List<UserPermission> userPermissions) {
		this.userPermissions = userPermissions;
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}
	
}
