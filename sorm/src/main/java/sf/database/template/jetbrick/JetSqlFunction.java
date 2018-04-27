package sf.database.template.jetbrick;

import jetbrick.template.JetAnnotations;
import jetbrick.template.runtime.InterpretContext;
import sf.database.template.TemplateConstants;

import java.util.List;

@JetAnnotations.Functions
public class JetSqlFunction {

    public static String para(Object value) {
        InterpretContext ctx = InterpretContext.current();
        List<Object> list = (List<Object>) ctx.getValueStack().getValue(TemplateConstants.LIST_KEY);
        list.add(value);
        return "?";
    }

    public static String p(Object obj) {
        return para(obj);
    }

    public static String in(List<Object> values) {
        InterpretContext ctx = InterpretContext.current();
        List<Object> list = (List<Object>) ctx.getValueStack().getValue(TemplateConstants.LIST_KEY);
        String ret = "";
        if (values != null && !values.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("in(");
            boolean f = false;
            for (Object val : values) {
                sb.append(f ? "," : "").append("?");
                list.add(val);
                f = true;
            }
            sb.append(")");
            ret = sb.toString();
        }
        return ret;
    }

    public static String exists(List<Object> values) {
        InterpretContext ctx = InterpretContext.current();
        List<Object> list = (List<Object>) ctx.getValueStack().getValue(TemplateConstants.LIST_KEY);
        String ret = "";
        if (values != null && !values.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("exists(");
            boolean f = false;
            for (Object val : values) {
                sb.append(f ? "," : "").append("?");
                list.add(val);
                f = true;
            }
            sb.append(")");
            ret = sb.toString();
        }
        return ret;
    }
}
