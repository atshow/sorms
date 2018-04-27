package sf.database.template.jetbrick;

import jetbrick.template.JetAnnotations;
import jetbrick.template.runtime.JetTagContext;
import sf.database.template.sql.SqlHelp;
import sf.tools.StringUtils;
import sf.tools.utils.Assert;

import java.io.IOException;

@JetAnnotations.Tags
public class JetSqlTag {
    public static void where(JetTagContext ctx) throws IOException {
        String content = ctx.getBodyContent();
        if (StringUtils.isNotBlank(content)) {
            content = SqlHelp.compressSql(content);
            content = content.trim();
            if (content.startsWith("and")) {
                content = " where " + content.substring(content.indexOf("and") + 3, content.length());
            } else {
                content = " where " + content;
            }
        }
        ctx.getWriter().print(content);
    }

    /**
     * 加载sql语句
     * @param ctx
     * @param name
     * @throws IOException
     */
    public static void loadSql(JetTagContext ctx, String name) {
        Assert.notNull(name, "");
        String context = ctx.getBodyContent();
        Assert.notNull(context, "");
        JetbrickHandler.sqlTemplate.put(name, context);
    }
}
