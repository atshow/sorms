package sf.database.dbinfo;

import sf.database.dialect.DBDialect;
import sf.database.meta.ColumnMapping;
import sf.database.support.RDBMS;
import sf.tools.StringUtils;

import javax.persistence.GenerationType;
import java.util.ArrayList;
import java.util.List;

public class ColumnDBType {
    protected boolean nullable = true;
    protected boolean unique = false;

    public Object defaultValue;

    private int sqlType;

    public ColumnDBType() {
    }

    public ColumnDBType(ColumnMapping cm) {
        if (cm.getColumn() != null) {
            this.nullable = cm.getColumn().nullable();
            this.unique = cm.getColumn().unique();
            Object o = null;
            //TODO 这个不对
            this.defaultValue = cm.getColumn().columnDefinition();
        }
        this.sqlType = cm.getSqlType();
    }

    /**
     * 比较列定义， 一样就返回true
     * @param c
     * @param profile
     * @return
     */
    public static List<ColumnChange> isEqualTo(ColumnMapping cm, ColumnInfo c, DBDialect profile) {
        ColumnDBType oldType = c.toColumnType(profile);
        ColumnDBType newType = new ColumnDBType(cm);
        List<ColumnChange> result = new ArrayList<ColumnChange>();

        // 对自增类型的数据不检查缺省值(兼容PG)
        if (!(cm.getGv() != null &&
                ((cm.getGv().strategy() == GenerationType.AUTO ||
                        cm.getGv().strategy() == GenerationType.IDENTITY ||
                        cm.getGv().strategy() == GenerationType.SEQUENCE) &&
                        Number.class.isAssignableFrom(cm.getClz())))) {
            // 检查缺省值
            String a1 = oldType.defaultValue == null ? null : oldType.defaultValue.toString();
            String a2 = newType.defaultValue == null ? null : newType.defaultValue.toString();
            // 非字符串比较情况下全部按小写处理
//			if (a1 != null && !a1.startsWith("'")) {
//				a1 = StringUtils.lowerCase(a1);
//			}
//			if (a2 != null && !a2.startsWith("'")) {
//				a2 = StringUtils.lowerCase(a2);
//			}
            if (!StringUtils.equals(a1, a2)) {
                ColumnChange chg;
                if (StringUtils.isEmpty(a2)) {
                    chg = new ColumnChange(ColumnChange.Change.CHG_DROP_DEFAULT);
                } else {
                    chg = new ColumnChange(ColumnChange.Change.CHG_DEFAULT);
                }
                chg.setFrom(a1);
                chg.setTo(a2);
                result.add(chg);
            }
        }

        // 针对NUll特性检查
        if (oldType.nullable != newType.nullable) {
            if (newType.nullable) {
                result.add(new ColumnChange(ColumnChange.Change.CHG_TO_NULL));
            } else {
                result.add(new ColumnChange(ColumnChange.Change.CHG_TO_NOT_NULL));
            }
        }

        // 再检查数据类型
        if (cm.getClz() == Boolean.class) {// 长度为1的字符或数字都算匹配.目前对boolean的处理较为含糊
            if (c.getColumnSize() == 1) {
                return result;// 不用再比了。
            }
        }
        if (profile.getName() == RDBMS.oracle) {// 很特殊的情况,Oracle下不映射其Timestamp类型，因此Oracle的Date和TimeStamp即被认为是等效的
            return result;// 不用再比了，认为数据类型一样
        }
        if (c.getSqlType() == newType.getSqlType()) {
            //TODO
        } else {

        }
        return result;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }
}
