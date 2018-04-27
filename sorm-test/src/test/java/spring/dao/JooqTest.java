package spring.dao;

import db.domain.Org;
import db.domain.User;
import db.domain.User.Field;
import org.jooq.SQLDialect;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.jooq.impl.IdentifiersHelp;
import org.jooq.impl.JooqRecord;
import org.jooq.impl.SQLDataType;
import org.jooq.util.GenerationTool;
import org.junit.Test;
import sf.jooq.JooqTables;
import sf.jooq.tables.JooqDynamicTable;
import sf.jooq.tables.JooqTable;

import java.io.File;

public class JooqTest {
    @Test
    public void t1() throws Exception {
        String path = Thread.currentThread().getClass().getResource("/").getPath();
        System.out.println(path);
        GenerationTool.main(new String[]{path + File.separator + "library.xml"});
    }

    @Test
    public void t2() throws Exception {
        JooqTable<?> quser = JooqTables.getTable(User.class);

        Select s = DSL.selectFrom(quser).where(quser.column(Field.id).in(1, 2, 3, 4)
                .or(quser.column(Field.nicename).eq("2").and(quser.column(Field.deleted).eq(false))))
                .limit(1).offset(2);
        System.out.println(s.getSQL());
    }

    @Test
    public void t3() throws Exception {
        JooqTable<?> quser = JooqTables.getTable(User.class);

        Select s = DSL.selectFrom(quser).where(quser.c(Field.id).in(1, 2, 3, 4)
                .or(quser.c(Field.nicename).eq("2").and(quser.c(Field.deleted).eq(false))))
                .limit(1).offset(2);
        System.out.println(s.getSQL());

        JooqTable<JooqRecord> qorg = (JooqTable<JooqRecord>) JooqTables.getTable(Org.class);
        long start = System.currentTimeMillis();
        JooqTable<JooqRecord> t = qorg.as("t");
        JooqTable<JooqRecord> r = qorg.as("r");
        System.out.println(System.currentTimeMillis() - start + "ms");
        start = System.currentTimeMillis();
        s = DSL.withRecursive("result").as(DSL.selectFrom(t.as("t")).where(t.c(Org.Field.id).eq("1"))
                .union(DSL.selectFrom(r).where(r.column(Org.Field.id).eq(t.c(Org.Field.parentId))))).select().from(DSL.name("result"));
        System.out.println(System.currentTimeMillis() - start + "ms");
        System.out.println(s.getSQL());
        IdentifiersHelp.addOuterDB();
        SQLDialect sqlserver = SQLDialect.valueOf(IdentifiersHelp.SQLSERVER);
        s = DSL.using(SQLDialect.valueOf(IdentifiersHelp.SQLSERVER)).select().from(t).startWith(t.c(Org.Field.id).eq(1)).connectBy(t.c(Org.Field.id).eq(t.c(Org.Field.parentId)));
        System.out.println(s.getSQL());
    }

    @Test
    public void t4() throws Exception {
        JooqDynamicTable table = new JooqDynamicTable("org");
        table.addColumn("aks", SQLDataType.VARCHAR(255), "");
        table.addColumn("sss", SQLDataType.VARCHAR(255), "");
        JooqDynamicTable table1 = table.as("ls");
        table1.removeColumn("sss");

        System.out.println(DSL.selectFrom(table).getSQL());
        System.out.println(DSL.selectFrom(table1).getSQL());
    }
}
