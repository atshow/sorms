package db.domain;

import sf.database.DBCascadeField;
import sf.database.DBField;
import sf.database.annotations.FetchDBField;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 角色表
 *
 * @author shixiafeng
 */
@Entity
@Table(name = "role")
public class Role extends PublicField {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    /**
     *角色必须以ROLE_开头
     */
    @Column(name = "role", length = 200)
    private String role;

    @Column
    @Lob
    private String description;

    @Column
    private Boolean available;

//    @ManyToMany
//    @JoinTable(name = "role_priv", joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")}, inverseJoinColumns = {@JoinColumn(name = "privilege_id", referencedColumnName = "id")})
    @Transient
    private List<Privilege> privileges;

    @ManyToMany(mappedBy = "roles")
    @OrderBy("id asc,loginName desc")
    private List<User> users;

    @ManyToMany(mappedBy = "roles")
    @OrderBy("id asc,loginName desc")
    @FetchDBField({"id","loginName"})
    private List<User> users2;

    public enum Field implements DBField {
        id, role, description, available,createBy,created,modifyBy,modified,ownerOrg,dataValid;
    }

    public enum CascadeField implements DBCascadeField{
        users,users2
    }

    public Role() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public List<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getModifyBy() {
        return modifyBy;
    }

    public void setModifyBy(String modifyBy) {
        this.modifyBy = modifyBy;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getOwnerOrg() {
        return ownerOrg;
    }

    public void setOwnerOrg(String ownerOrg) {
        this.ownerOrg = ownerOrg;
    }

    public Boolean getDataValid() {
        return dataValid;
    }

    public void setDataValid(Boolean dataValid) {
        this.dataValid = dataValid;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<User> getUsers2() {
        return users2;
    }

    public void setUsers2(List<User> users2) {
        this.users2 = users2;
    }
}
