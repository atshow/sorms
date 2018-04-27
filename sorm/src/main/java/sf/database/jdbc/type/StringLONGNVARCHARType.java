package sf.database.jdbc.type;

import java.sql.Types;

public class StringLONGNVARCHARType extends StringVARCHARType {
    @Override
    public int getSqlType() {
        return Types.LONGNVARCHAR;
    }
}
