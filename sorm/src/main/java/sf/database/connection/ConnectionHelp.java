package sf.database.connection;

import com.alibaba.druid.pool.DruidPooledConnection;
import sf.tools.utils.ClassUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionHelp {
    private static boolean druidExist = ClassUtils.isPresent("com.alibaba.druid.pool.DruidPooledConnection", ConnectionHelp.class.getClassLoader());

    /**
     * 还原为原始连接
     * @param wrap
     * @return
     */
    public static Connection getPhysicalConnection(Connection wrap) {
        if (druidExist) {
            if (wrap instanceof DruidPooledConnection) {
                DruidPooledConnection dpc = (DruidPooledConnection) wrap;
                try {
                    return dpc.unwrap(Connection.class);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return wrap;
    }
}
