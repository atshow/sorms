package db.domain;

import sf.database.DBCascadeField;
import sf.database.DBField;

import javax.persistence.*;
import java.util.List;

/**
 * 权限表
 *
 * @author shixiafeng
 */
@Entity
@Table(name = "privilege")
@Cacheable
//@BindDataSource("sqlite")
public class Privilege extends PublicField {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Column
    private String name;
    @Column
    private String type;
    @Column
    private String url;
    @Column
    private String ico;
    @Column
    private Integer sequence;

    @Column
    private String parentId;

    private String parentIds;

    @Column
    private String permission;
    @Column
    private Boolean available;

    @ManyToMany(cascade = CascadeType.REFRESH)
    @JoinTable(name = "role_to_priv", inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")}, joinColumns = {@JoinColumn(name = "privilege_id", referencedColumnName = "id")})
    private List<Role> roles;

    public  enum Field implements DBField{
        id, name, type, url, parentId, parentIds, permission, available, ico, sequence,createBy,created,modifyBy,modified,ownerOrg,dataValid;
    }

    public Privilege() {

    }

    public enum CascadeField implements DBCascadeField{
        roles
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getIco() {
        return ico;
    }

    public void setIco(String ico) {
        this.ico = ico;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentIds() {
        return parentIds;
    }

    public void setParentIds(String parentIds) {
        this.parentIds = parentIds;
    }
}
