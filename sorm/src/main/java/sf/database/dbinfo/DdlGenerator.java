package sf.database.dbinfo;

import sf.database.meta.ColumnMapping;
import sf.database.meta.TableMapping;
import sf.database.meta.def.IndexDef;

import java.util.List;
import java.util.Set;

public interface DdlGenerator {
    // ///////////////////////////////////////////////////////////////

    /**
     * 转为建表语句
     * @param obj
     * @param tablename
     * @return
     */
    String toTableCreateClause(TableMapping obj, String tablename);

    /**
     * 转为索引语句
     * @param obj
     * @param tablename
     * @return
     */
    List<String> toIndexClause(TableMapping obj, String tablename);

    /**
     * 生成Alter table 语句
     * @return
     */
    List<String> toTableModifyClause(TableMapping meta, String tableName, Set<ColumnMapping> insert, List<ColumnMapping> changed,
                                     List<String> delete);

    /**
     * 生成 create view语句
     * @return
     */
    List<String> toViewCreateClause();

    /**
     * 生成删除约束的语句
     * @return
     */
    String getDropConstraintSql(String tableName, String contraintName);

    String addIndex(IndexDef index, TableMapping meta, String tablename);

    String addConstraint(Constraint con);
}
