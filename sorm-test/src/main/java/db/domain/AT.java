package db.domain;

import sf.database.annotations.Type;
import sf.database.annotations.UniqueKeyGenerator;
import sf.database.jdbc.extension.ObjectJsonMapping;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * 用户类
 *
 * @author SXF
 */
@Entity
@Table(name = "wp_at")
public class AT implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @UniqueKeyGenerator(targetClass = AtCustomKey.class)//自定义主键生成.
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
    private Integer status;

    @Column(name = "display_name", length = 250)
    @Enumerated(EnumType.STRING)
    private Names displayName;

    @Column
    private Boolean spam;

    @Column
    private Boolean deleted;

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
    @OneToMany(targetEntity = UserMeta.class, mappedBy = "userId")
    @JoinColumn(name = "id", referencedColumnName = "userId")
    private Set<UserMeta> userMetaSet = new LinkedHashSet<UserMeta>();

//    @OneToOne(targetEntity = UserMeta.class, mappedBy = "userId")
//    @JoinColumn(name = "id", referencedColumnName = "userId")
//    private UserMeta um;


    public enum Names {
        zhangshang, lisi
    }

    public AT() {

    }

    public Long getId() {
        return id;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
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

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
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

    public Map<String, String> getMaps() {
        return maps;
    }

    public void setMaps(Map<String, String> maps) {
        this.maps = maps;
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", loginName=" + loginName + ", password=" + password + ", nicename=" + nicename
                + ", email=" + email + ", url=" + url + ", registered=" + registered + ", activationKey="
                + activationKey + ", status=" + status + ", displayName=" + displayName + ", spam=" + spam
                + ", deleted=" + deleted + ", lock=" + lock + ", roles=" + roles + ", userMetaSet=" + userMetaSet + "]";
    }
}
