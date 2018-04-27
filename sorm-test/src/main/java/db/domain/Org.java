package db.domain;

import sf.database.DBField;
import sf.database.DBObject;

import javax.persistence.*;

@Entity
@Table(name = "org")
@Cacheable
public class Org extends DBObject {
    @Id
    private String id;
    @Column(name = "parent_id")
    private String parentId;

    @Column
    private String name;

    public  enum Field implements DBField {
        id, parentId, name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
