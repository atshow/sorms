package sf.database.jdbc.type;

import java.sql.Types;

public class ByteArrayVARBINARYType extends ByteArrayBINARYType {
    @Override
    public int getSqlType() {
        return Types.VARBINARY;
    }
}
