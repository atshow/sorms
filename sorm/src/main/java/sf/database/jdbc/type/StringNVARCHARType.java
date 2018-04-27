package sf.database.jdbc.type;

import java.sql.Types;

public class StringNVARCHARType extends StringVARCHARType {
    @Override
    public int getSqlType() {
        return Types.NVARCHAR;
    }
}
