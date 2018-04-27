package sf.database.template.jetbrick;

import jetbrick.template.JetEngine;
import jetbrick.template.resolver.GlobalResolver;

import java.util.Properties;

public class JetHelp {
    private static JetEngine engine;

    public static JetEngine getJetEngine() {
        if (engine == null) {
            Properties props = new Properties();
            engine = JetEngine.create(props);
            GlobalResolver resolver = engine.getGlobalResolver();
            resolver.registerFunctions(JetSqlFunction.class);
            resolver.registerTags(JetSqlTag.class);
        }
        return engine;
    }
}
