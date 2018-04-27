package sf.database.jdbc.type;

import java.sql.Types;

public class DoubleFLOATType extends DoubleDOUBLEType {
    @Override
    public int getSqlType() {
        return Types.FLOAT;
    }
}
