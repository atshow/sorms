package db.domain;

import sf.database.DBCascadeField;
import sf.database.DBField;
import sf.database.DBObject;
import sf.database.annotations.FetchDBField;

import javax.persistence.*;
import java.util.Date;

/**
 * @author SXF
 * 
 */
@Entity
@Table(name = "wp_usermeta")
public class UserMeta extends DBObject {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "`key`")
	private String key;
	@Column
	private String value;

	@Column
	@Version
	private String version;

	@Column
	@Version
	private int version2;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	@Version
	private Date version3;

	@ManyToOne
	@JoinColumn(name = "userId",referencedColumnName = "id")
	private User user;

	@OneToOne(mappedBy = "userMetaSet")
	@FetchDBField({"id","loginName"})
	private User user2;

	public enum Field implements DBField {
		id, userId, key, value,version,version2,version3;
	}

	public enum CascadeField implements DBCascadeField{
		user,user2
	}

	public UserMeta() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserMeta other = (UserMeta) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser2() {
		return user2;
	}

	public void setUser2(User user2) {
		this.user2 = user2;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getVersion2() {
		return version2;
	}

	public void setVersion2(int version2) {
		this.version2 = version2;
	}

	public Date getVersion3() {
		return version3;
	}

	public void setVersion3(Date version3) {
		this.version3 = version3;
	}
}
