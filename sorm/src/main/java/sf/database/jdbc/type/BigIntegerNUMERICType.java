package sf.database.jdbc.type;

import java.sql.Types;

public class BigIntegerNUMERICType extends BigIntegerBIGINTType {
    @Override
    public int getSqlType() {
        return Types.NUMERIC;
    }

}
