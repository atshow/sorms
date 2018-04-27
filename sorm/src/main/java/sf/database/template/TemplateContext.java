package sf.database.template;

import java.util.HashMap;
import java.util.Map;

/**
 * 模板引擎执行上下文
 */
public class TemplateContext {
    protected final static ThreadLocal<TemplateContext> threadLocal = new InheritableThreadLocal<TemplateContext>();


    public static TemplateContext current() {
        return threadLocal.get();
    }


    public void setThreadLocal() {
        threadLocal.set(this);
    }

    public void freeThreadLocal() {
        threadLocal.remove();
    }

    /**
     * 保存相关的值
     */
    protected Map<String, Object> values = new HashMap<>();

    public Map<String, Object> getValues() {
        return values;
    }
}
