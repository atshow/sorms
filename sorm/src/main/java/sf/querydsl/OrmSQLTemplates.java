package sf.querydsl;

import com.querydsl.sql.SQLTemplates;

import java.util.Set;

/**
 * 去除特殊字符的处理.
 */
public class OrmSQLTemplates extends SQLTemplates {
    public OrmSQLTemplates() {
        this("", '\\', false);
    }

    @Deprecated
    public OrmSQLTemplates(String quoteStr, char escape, boolean useQuotes) {
        super("", escape, useQuotes);
    }

    public OrmSQLTemplates(Set<String> reservedKeywords, String quoteStr, char escape, boolean useQuotes) {
        super(reservedKeywords, "", escape, useQuotes);
    }
}
