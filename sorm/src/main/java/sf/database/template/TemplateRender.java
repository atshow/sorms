package sf.database.template;

import sf.database.template.jetbrick.JetbrickHandler;

public class TemplateRender {
    private static TemplateHandler jetbrick = JetbrickHandler.getInstance();

    public static String DEFAULT_TEMPLATE = "";

    public static TemplateHandler getTemplateHandler() {
        //jetbrick
        return jetbrick;
    }
}
