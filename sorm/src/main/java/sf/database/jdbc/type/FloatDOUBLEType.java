package sf.database.jdbc.type;

import java.sql.Types;

public class FloatDOUBLEType extends FloatREALType {
    @Override
    public int getSqlType() {
        return Types.DOUBLE;
    }
}
