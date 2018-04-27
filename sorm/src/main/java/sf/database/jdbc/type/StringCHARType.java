package sf.database.jdbc.type;

import java.sql.Types;

public class StringCHARType extends StringVARCHARType {
    @Override
    public int getSqlType() {
        return Types.CHAR;
    }
}
