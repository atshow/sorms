package sf.database.util;

public class SQLUtils {
    /**
     * 去除关键字的特殊字符
     * @param columnName
     * @return
     */
    public static String getNoWrapperColumnName(String columnName) {
        if (columnName.startsWith("`") && columnName.endsWith("`")) {
            columnName = columnName.replace("`", "");
        } else if (columnName.startsWith("\"") && columnName.endsWith("\"")) {
            columnName = columnName.replace("\"", "");
        } else if (columnName.startsWith("[") && columnName.endsWith("]")) {
            columnName = columnName.replace("[", "").replace("]", "");
        }
        return columnName;
    }
}
