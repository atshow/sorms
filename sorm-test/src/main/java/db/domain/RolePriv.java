package db.domain;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

//@Entity
//@Table(name = "role_priv")
public class RolePriv implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	@Id
//	@GeneratedValue
//	private Integer id;

	@Id
	@Column(name = "role_id")
	private String roleId;

	@Id
	@Column(name = "privilege_id")
	private String privilegeId;

	public RolePriv() {
	}

//	public Integer getId() {
//		return id;
//	}

//	public void setId(Integer id) {
//		this.id = id;
//	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getPrivilegeId() {
		return privilegeId;
	}

	public void setPrivilegeId(String privilegeId) {
		this.privilegeId = privilegeId;
	}
}
