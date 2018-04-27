package sf.ext.gen;

import jetbrick.template.JetEngine;
import jetbrick.template.JetTemplate;
import sf.database.dbinfo.ColumnInfo;
import sf.database.dbinfo.DBMetaData;
import sf.database.dbinfo.ObjectType;
import sf.database.dbinfo.TableInfo;
import sf.database.jdbc.type.JavaType;
import sf.database.template.jetbrick.JetHelp;
import sf.database.util.DBUtils;
import sf.dsl.Operator;
import sf.tools.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Types;
import java.util.*;

public class PojoGen {
    Connection conn;
    String pkg;
    String srcPath;
    GenConfig config;
    public static StringBuilder srcHead = new StringBuilder();
    public static String defaultPkg = "com.test";
    static String CR = System.getProperty("line.separator");

    public static JetEngine gt = JetHelp.getJetEngine();

    static {
        srcHead.append("import java.math.*;" + CR);
        srcHead.append("import java.util.Date;" + CR);
        srcHead.append("import java.sql.Timestamp;" + CR);
        srcHead.append("import javax.persistence.*;" + CR);
        srcHead.append("import sf.database.annotations.*;" + CR);
    }

    public PojoGen(Connection conn, String pkg, String srcPath, GenConfig config) {
        this.conn = conn;
        this.pkg = pkg;
        this.srcPath = srcPath;
        this.config = config;
    }

    /**
     * 生成代码
     */
    public void gen() throws Exception {
        List<TableInfo> tableInfos = DBMetaData.getInstance().getDatabaseObject(conn, ObjectType.TABLE, DBUtils.getCatalog(conn), DBUtils.getSchema(conn), null, Operator.EQUALS);
        for (TableInfo table : tableInfos) {
            List<ColumnInfo> columnInfos = DBMetaData.getInstance().getColumns(conn, table.getName());
            table.setColumnInfos(columnInfos);
            gen(table);
        }
    }

    public void gen(TableInfo info) throws Exception {

        String className = StringUtils.firstCharToUpperCase(StringUtils.toCamelCase(info.getName()));
        String ext = null;

        if (config.getBaseClass() != null) {
            ext = config.getBaseClass();
        }

        List<ColumnInfo> cols = info.getColumnInfos();
        List<Map> attrs = new ArrayList<Map>();
        for (ColumnInfo col : cols) {

            Map attr = new LinkedHashMap();
            attr.put("comment", col.getRemarks());
            String attrName = StringUtils.toCamelCase(col.getColumnName());
            attr.put("name", attrName);
            attr.put("methodName", getMethodName(attrName));
            attr.put("columnName", col.getColumnName());

            //设置java类型
            attr.put("type", col.getRemarks());
            String type = JavaType.getType(col.getSqlType(), col.getColumnSize(), col.getDecimalDigit());
            if (config.isPreferBigDecimal() && type.equals("Double")) {
                type = "BigDecimal";
            }
            if (config.isPreferDate() && type.equals("Timestamp")) {
                type = "Date";
            }
            attr.put("type", type);

            //是否为主键
            boolean pk = false;
            if (info.getPrimaryKey().contains(col.getColumnName())) {
                pk = true;
            }
            attr.put("pk", pk);

            //设置日期类型
            String temporal = null;
            if (col.getSqlType() == Types.DATE) {
                temporal = "@Temporal(TemporalType.DATE)";
            } else if (col.getSqlType() == Types.TIMESTAMP || col.getSqlType() == Types.TIMESTAMP_WITH_TIMEZONE) {
                temporal = "@Temporal(TemporalType.TIMESTAMP)";
            } else if (col.getSqlType() == Types.TIME || col.getSqlType() == Types.TIME_WITH_TIMEZONE) {
                temporal = "@Temporal(TemporalType.TIME)";
            }
            attr.put("temporal", temporal);

            //是否是lob对象
            boolean isLob = false;
            if (JavaType.isLob(col.getSqlType())) {
                isLob = true;
            }
            if ("String".equals(type) && col.getColumnSize() > 5000) {
                isLob = true;
            }
            attr.put("lob", isLob);

            //是否是大数据
            boolean bigNumber = false;
            if (col.getSqlType() == Types.NUMERIC || col.getSqlType() == Types.DECIMAL) {
                bigNumber = true;
            }
            attr.put("bigNumber", bigNumber);


            attr.put("desc", col);
            attrs.add(attr);
        }

        if (config.getPropertyOrder() == config.ORDER_BY_TYPE) {
            // 主键总是拍在前面，int类型也排在前面，剩下的按照字母顺序排
            Collections.sort(attrs, new Comparator<Map>() {

                @Override
                public int compare(Map o1, Map o2) {
                    ColumnInfo desc1 = (ColumnInfo) o1.get("desc");
                    ColumnInfo desc2 = (ColumnInfo) o2.get("desc");
                    int score1 = score(desc1);
                    int score2 = score(desc2);
                    if (score1 == score2) {
                        return desc1.getColumnName().compareTo(desc2.getColumnName());
                    } else {
                        return score2 - score1;
                    }
                }

                private int score(ColumnInfo desc) {
                    if (info.getPrimaryKey().contains(desc.getColumnName())) {
                        return 99;
                    } else if (JavaType.isInteger(desc.getSqlType())) {
                        return 9;
                    } else if (JavaType.isDateType(desc.getSqlType())) {
                        return -9;
                    } else {
                        return 0;
                    }
                }


            });
        }


        JetTemplate template = gt.createTemplate(config.template);
        Map<String, Object> map = new HashMap<>();
        map.put("attrs", attrs);
        map.put("className", className);
        map.put("tableName", info.getName());
        map.put("ext", ext);
        map.put("packages", pkg);
        map.put("imports", srcHead.toString());
        map.put("comment", info.getRemarks());
        map.put("table",info);
        StringWriter writer = new StringWriter();
        template.render(map, writer);
        String code = writer.toString();
        if (config.isDisplay()) {
            System.out.println(code);
        } else {
            saveSourceFile(srcPath, pkg, className, code);
        }

        for (CodeGen codeGen : config.codeGens) {
            codeGen.genCode(pkg, className, info, config, config.isDisplay());
        }


    }

    public static void saveSourceFile(String srcPath, String pkg, String className, String content) throws IOException {
        String file = srcPath + File.separator + pkg.replace('.', File.separatorChar);
        File f = new File(file);
        f.mkdirs();
        File target = new File(file, className + ".java");
        FileWriter writer = new FileWriter(target);
        writer.write(content);
        writer.close();
    }

    private String getMethodName(String name) {
        if (name.length() == 1) {
            return name.toUpperCase();
        }
        char ch1 = name.charAt(0);
        char ch2 = name.charAt(1);
        if (Character.isLowerCase(ch1) && Character.isUpperCase(ch2)) {
            //aUname---> getaUname();
            return name;
        } else if (Character.isUpperCase(ch1) && Character.isUpperCase(ch2)) {
            //ULR --> getURL();
            return name;
        } else {
            //general  name --> getName()
            char upper = Character.toUpperCase(ch1);
            return upper + name.substring(1);
        }
    }

}

