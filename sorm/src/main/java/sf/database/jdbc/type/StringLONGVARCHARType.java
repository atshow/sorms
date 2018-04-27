package sf.database.jdbc.type;

import java.sql.Types;

public class StringLONGVARCHARType extends StringVARCHARType {
    @Override
    public int getSqlType() {
        return Types.LONGVARCHAR;
    }
}
