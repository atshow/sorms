package db.domain;

import sf.database.DBCascadeField;
import sf.database.DBField;
import sf.database.annotations.Comment;
import sf.database.annotations.FetchDBField;
import sf.database.annotations.Type;
import sf.database.jdbc.extension.ObjectJsonMapping;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * 用户类
 *
 * @author SXF
 */
@Entity
@Table(name = "wp_users")
@Comment("用户表")
public class User extends PublicField {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "login_name", length = 60, nullable = false)
    private String loginName;// 登陆名

    @Column(length = 64)
    private String password;

    @Column(length = 50)
    private String nicename;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String url;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date registered;

    /**
     * 激活码
     */
    @Column(name = "activation_key", length = 60, nullable = false)
    private String activationKey;

    @Column
    private int status;

    @Column(name = "display_name", length = 250)
    @Enumerated(EnumType.STRING)
    private Names displayName;

    @Column
    private Boolean spam;

    @Column
    private boolean deleted;

    @Column(precision = 10,scale = 5)
    private BigDecimal weight;

    @Transient
    private boolean lock;

    @Column(name = "maps",length = 1500)
    @Type(ObjectJsonMapping.class)
    private Map<String,String> maps;

    @ManyToMany
    @Transient
    @OrderBy("id asc,role desc")
    @JoinTable(name = "user_role", joinColumns = {
            @JoinColumn(name = "user_id", referencedColumnName = "id")}, inverseJoinColumns = {
            @JoinColumn(name = "role_id", referencedColumnName = "id")})
    private List<Role> roles;


    @OrderBy
    @Transient
    @FetchDBField({"id","key"})
    @OneToMany(targetEntity = UserMeta.class)
    @JoinColumn(name = "id", referencedColumnName = "userId")
    private Set<UserMeta> userMetaSet = new LinkedHashSet<UserMeta>();

//    @OneToOne(targetEntity = UserMeta.class, mappedBy = "userId")
//    @JoinColumn(name = "id", referencedColumnName = "userId")
//    private UserMeta um;


    public enum Names {
        zhangshang, lisi
    }

    /**
     * 普通字段
     */
    public enum Field implements DBField {
      id, loginName, password, nicename, email, url, registered, activationKey, status, displayName,maps, spam, deleted,weight, createBy, created, modifyBy, modified, ownerOrg, dataValid;
    }

    /**
     * 级联字段
     */
    public enum CascadeField implements DBCascadeField {
        roles, userMetaSet
    }

    public User() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (this._recordUpdate) {
            this.prepareUpdate(User.Field.id, id);
        }
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNicename() {
        return nicename;
    }

    public void setNicename(String nicename) {
        this.nicename = nicename;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getRegistered() {
        return registered;
    }

    public void setRegistered(Date registered) {
        this.registered = registered;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Names getDisplayName() {
        return displayName;
    }

    public void setDisplayName(Names displayName) {
        this.displayName = displayName;
    }

    public Boolean getSpam() {
        return spam;
    }

    public void setSpam(Boolean spam) {
        this.spam = spam;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public Set<UserMeta> getUserMetaSet() {
        return userMetaSet;
    }

    public void setUserMetaSet(Set<UserMeta> userMetaSet) {
        this.userMetaSet = userMetaSet;
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

    public Map<String, String> getMaps() {
        return maps;
    }

    public void setMaps(Map<String, String> maps) {
        this.maps = maps;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", loginName=" + loginName + ", password=" + password + ", nicename=" + nicename
                + ", email=" + email + ", url=" + url + ", registered=" + registered + ", activationKey="
                + activationKey + ", status=" + status + ", displayName=" + displayName + ", spam=" + spam
                + ", deleted=" + deleted + ", lock=" + lock + ", roles=" + roles + ", userMetaSet=" + userMetaSet + "]";
    }
}
