package sf.database.template.jetbrick;

import jetbrick.template.JetTemplate;
import sf.database.template.TemplateConstants;
import sf.database.template.TemplateHandler;
import sf.database.template.sql.SQLContext;
import sf.database.template.sql.SQLParameter;
import sf.database.template.sql.SqlHelp;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JetbrickHandler implements TemplateHandler {
    // key:名称,value:sql语句
    public static final Map<String, String> sqlTemplate = new ConcurrentHashMap<String, String>();

    /**
     * 默认模板位置,处于class下
     */
    public static String DEFAULT_TEMPLATE = "/jetx.sql";

    private static boolean initialized = false;

    private static boolean debug = false;

    private static JetbrickHandler instance;

    public static JetbrickHandler getInstance() {
        if (instance == null) {
            instance = new JetbrickHandler();
        }
        return instance;
    }


    private JetbrickHandler() {

    }


    @Override
    public void loadAllSQL() {
        sqlTemplate.clear();
        JetTemplate jt = JetHelp.getJetEngine().getTemplate(DEFAULT_TEMPLATE);
        StringWriter writer = new StringWriter();
        List<Object> values = new ArrayList<>();
        Map<String, Object> context = new HashMap<>();
        context.put(TemplateConstants.LIST_KEY, values);
        jt.render(context, writer);
        initialized = true;
    }

    @Override
    public String getSQL(String sqlId) {
        if (!initialized) {
            loadAllSQL();
            if (!debug) {// 非debug模式则初始化一次
                initialized = true;
            }
        }
        return sqlTemplate.get(sqlId);
    }

    @Override
    public SQLContext getParsedSql(String sqlId, Map<String, Object> paramters) {
        String orgSql = getSQL(sqlId);
        SQLContext parsedSql = new SQLContext();

        // 1. 创建一个默认的 JetEngine
        // 2. 获取一个模板对象 (从默认的 classpath 下面)
        JetTemplate template = JetHelp.getJetEngine().createTemplate(orgSql);
        // 3. 创建 context 对象
        Map<String, Object> context = new HashMap<>();
        if (paramters != null) {
            context.putAll(paramters);
        }
        List<Object> values = new ArrayList<>();
        context.put(TemplateConstants.LIST_KEY, values);
        // 4. 渲染模板到自定义的 Writer
        StringWriter writer = new StringWriter();
        template.render(context, writer);
        parsedSql.setOriginalSql(orgSql);
        List<SQLParameter> list = new ArrayList<>();
        for (Object v : values) {
            SQLParameter sqlParameter = new SQLParameter().setValue(v);
            list.add(sqlParameter);
        }
        parsedSql.setParas(list);
        String afterSql = writer.toString();
        afterSql = SqlHelp.compressSql(afterSql);
        parsedSql.setAfterSql(afterSql);
        return parsedSql;
    }
}
