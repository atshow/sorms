package sf.database.template.sql;

import sf.tools.StringUtils;

/**
 *
 */
public class SqlHelp {
    /**
     * sql压缩
     * @param sql
     * @return
     */
    public static String compressSql(String sql) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(sql)) {
            sql = sql.replace("\n", "");
            String[] arr = sql.split("\\s+");
            for (String str : arr) {
                sb.append(str);
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
