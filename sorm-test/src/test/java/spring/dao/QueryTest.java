package spring.dao;

import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.oracle.OracleQuery;
import db.domain.Org;
import db.domain.User;
import db.domain.User.Field;
import org.junit.Test;
import sf.querydsl.DynamicSQLRelationalPath;
import sf.querydsl.QueryDSL;
import sf.querydsl.SQLRelationalPath;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class QueryTest {
    @Test
    public void t1() {
        SQLQuery<?> select = new SQLQuery();
        SQLRelationalPath<?> quser = QueryDSL.relationalPathBase(User.class);
        long start = System.currentTimeMillis();
        select.select(quser).from(quser).where(quser.column(Field.id).in(Arrays.asList(1, 2, 3))
                .or(quser.column(Field.nicename).eq("1").and(quser.column(Field.deleted).eq(false))));
        System.out.println(System.currentTimeMillis() - start + "ms");
        System.out.println(select.getSQL().getSQL());
        start = System.currentTimeMillis();
        select = new SQLQuery();
        select.select(quser).from(quser).where(quser.number(Field.id).in(1, 2, 3)
                .and(quser.string(Field.nicename).eq("1").or(quser.bool(Field.deleted).eq(false))));
        System.out.println(System.currentTimeMillis() - start + "ms");
        System.out.println(select.getSQL().getSQL());

        //迭代查询
        SQLRelationalPath<?> qorg = QueryDSL.relationalPathBase(Org.class);
        SQLQuery<Org>  select2 = new SQLQuery();
        start = System.currentTimeMillis();
        SQLRelationalPath<?> results = qorg.cloneNew("results");
        SQLRelationalPath<?> t = qorg.cloneNew("t");
        System.out.println(System.currentTimeMillis() - start + "ms");
        start = System.currentTimeMillis();
        List<SubQueryExpression<Org>> sq = new ArrayList<>();
        sq.add((SubQueryExpression<Org>) new SQLQuery<Org>().select(qorg).from(qorg).where(qorg.column(Org.Field.id).eq("1")));
        sq.add((SubQueryExpression<Org>) new SQLQuery<Org>().select(t).from(t).where(results.column(Org.Field.parentId).eq(t.column(Org.Field.id))));
        select2.withRecursive(results, new SQLQuery<Org>().unionAll(sq)).select(results).from(results);

        System.out.println(System.currentTimeMillis() - start + "ms");
        System.out.println("next:\n"+select2.getSQL().getSQL());
        OracleQuery<Org>  select3 = new OracleQuery(null);
        select3.select(qorg).from(qorg).startWith(qorg.column(Org.Field.id).eq(1)).connectBy(qorg.column(Org.Field.id).eq(qorg.column(Org.Field.parentId)));
        System.out.println(select3.getSQL().getSQL());
    }

    @Test
    public void t4() throws Exception {
        DynamicSQLRelationalPath table = new DynamicSQLRelationalPath("org");
        table.addColumn(ColumnMetadata.named("ask").withSize(255).ofType(Types.VARCHAR),String.class);
        table.addColumn(ColumnMetadata.named("aa").withSize(255).ofType(Types.VARCHAR),String.class);

        DynamicSQLRelationalPath table1 = table.cloneNew("ls");
        table1.removeColumns("aa");

        System.out.println(new SQLQuery<Org>().select(table).from(table).getSQL().getSQL());
        System.out.println(new SQLQuery<Org>().select(table1).from(table1).getSQL().getSQL());
    }
}
