package sf.database.jdbc.type;

import java.sql.Types;

public class ByteArrayLONGVARBINARYType extends ByteArrayBINARYType {
    @Override
    public int getSqlType() {
        return Types.LONGVARBINARY;
    }
}
