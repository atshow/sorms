package db.domain;

import sf.database.DBObject;
import sf.database.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

public abstract class PublicField extends DBObject {
    //公共字段
    @Comment("创建人员")
    @Column(name = "create_by", length = 32)
    protected String createBy;

    @Comment("创建时间")
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    protected Date created;

    @Comment("修改人员")
    @Column(name = "modify_by", length = 32)
    protected String modifyBy;

    @Comment("修改时间")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date modified;

    @Comment("所属组织")
    @Column(name = "owner_org", length = 32)
    protected String ownerOrg;

    @Comment("true：有效 false：无效（逻辑删除）;默认为有效")
    @Column(name = "data_valid", columnDefinition = " boolean DEFAULT true ")
    protected Boolean dataValid = Boolean.TRUE;


}
