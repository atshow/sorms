package db.domain;

import sf.database.DBField;
import sf.database.DBObject;

import javax.persistence.Column;
import javax.persistence.Id;

//@Entity
//@Table(name = "user_role")
public class UserRole extends DBObject {
	private static final long serialVersionUID = 1L;

//	@Id
//	@GeneratedValue
//	private Integer id;
	@Id
	@Column(name = "user_id")
	private String userId;
	/**
	 *
	 */
	@Id
	@Column(name = "role_id")
	private String roleId;

	public enum Field implements DBField {
		userId, roleId;
	}

	public UserRole() {
		
	}

//	public Integer getId() {
//		return id;
//	}

//	public void setId(Integer id) {
//		this.id = id;
//	}


	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}


}
