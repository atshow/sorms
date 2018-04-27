package sf.database.jdbc.type;

import java.sql.Types;

public class BooleanBOOLEANType extends BooleanBITType {
    @Override
    public int getSqlType() {
        return Types.BOOLEAN;
    }
}
