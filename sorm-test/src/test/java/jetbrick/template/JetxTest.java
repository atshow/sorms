package jetbrick.template;

import jetbrick.template.resolver.GlobalResolver;
import sf.database.template.TemplateConstants;
import sf.database.template.jetbrick.JetSqlFunction;
import sf.database.template.jetbrick.JetSqlTag;

import java.io.StringWriter;
import java.util.*;

public class JetxTest {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        Properties props = new Properties();
        JetEngine  engine = JetEngine.create(props);
        GlobalResolver resolver = engine.getGlobalResolver();
        resolver.registerFunctions(JetSqlFunction.class);
        resolver.registerTags(JetSqlTag.class);
        JetTemplate jt = engine.getTemplate("/all.sql");
        StringWriter writer = new StringWriter();
        List<Object> values = new ArrayList<>();
        Map<String, Object> context = new HashMap<>();
        context.put(TemplateConstants.LIST_KEY,values);
        jt.render(context, writer);
        System.out.println(jt);
    }
}
