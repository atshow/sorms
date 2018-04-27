package sf.database.jdbc.type;

import java.sql.Types;

public class StringNCLOBType extends StringClobTypeHandler {
    @Override
    public int getSqlType() {
        return Types.NCLOB;
    }
}
