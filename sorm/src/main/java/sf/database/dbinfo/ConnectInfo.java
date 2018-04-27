package sf.database.dbinfo;

import sf.database.dialect.DBDialect;
import sf.tools.utils.Assert;

import javax.persistence.PersistenceException;


/**
 * 描述数据库连接的基本信息
 * @author Administrator
 */
public class ConnectInfo {
    // 三项基本信息
    String url;
    String user;
    String password;
    // 三项高级信息
    DBDialect profile;
    String dbname;
    String host;

    String databaseProductName;
    String databaseProductVersion;
    int databaseMajorVersion;
    String driverVersion;
    int driverMajorVersion;

    /**
     * 获得JDBC地址
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置地址
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获得用户名
     * @return
     */
    public String getUser() {
        return user;
    }

    /**
     * 设置用户名
     * @param user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * 获得口令
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置口令
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获得方言
     * @return database dialect
     */
    public DBDialect getProfile() {
        return profile;
    }

    /**
     * 设置方言
     * @param profile
     */
    public void setProfile(DBDialect profile) {
        this.profile = profile;
    }

    /**
     * 获得数据库名
     * @return 数据库名
     */
    public String getDbname() {
        return dbname;
    }

    /**
     * 设置数据库名
     * @param dbname
     */
    public void setDbname(String dbname) {
        if (profile != null) {
            dbname = profile.getIdentifier(dbname);
        }
        this.dbname = dbname;
    }

    /**
     * 获得数据库地址
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置数据库地址
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    public String getDatabaseProductName() {
        return databaseProductName;
    }

    public void setDatabaseProductName(String databaseProductName) {
        this.databaseProductName = databaseProductName;
    }

    public String getDatabaseProductVersion() {
        return databaseProductVersion;
    }

    public void setDatabaseProductVersion(String databaseProductVersion) {
        this.databaseProductVersion = databaseProductVersion;
    }

    public String getDriverVersion() {
        return driverVersion;
    }

    public void setDriverVersion(String driverVersion) {
        this.driverVersion = driverVersion;
    }

    public int getDriverMajorVersion() {
        return driverMajorVersion;
    }

    public void setDriverMajorVersion(int driverMajorVersion) {
        this.driverMajorVersion = driverMajorVersion;
    }

    public int getDatabaseMajorVersion() {
        return databaseMajorVersion;
    }

    public void setDatabaseMajorVersion(int databaseMajorVersion) {
        this.databaseMajorVersion = databaseMajorVersion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("type=").append(profile != null ? profile.getName() : null);
        sb.append("\thost=").append(host);
        sb.append("\tdb=").append(dbname);
        return sb.toString();
    }

    /**
     * 目前已知的所有JDBC URL开头和驱动之间的关系
     * jdbc:derby 	org.apache.derby.jdbc.EmbeddedDriver
     * jdbc:mysql 	com.mysql.jdbc.Driver
     * jdbc:oracle 	oracle.jdbc.driver.OracleDriver
     * jdbc:microsoft com.microsoft.jdbc.sqlserver.SQLServerDriver
     * jdbc:sybase:Tds com.sybase.jdbc2.jdbc.SybDriver
     * jdbc:jtds 		net.sourceforge.jtds.jdbc.Driver
     * jdbc:postgresql org.postgresql.Driver
     * jdbc:hsqldb org.hsqldb.jdbcDriver
     * jdbc:db2 COM.ibm.db2.jdbc.app.DB2Driver DB2的JDBC Driver十分混乱，这个匹配不一定对
     * jdbc:sqlite org.sqlite.JDBC
     * jdbc:ingres com.ingres.jdbc.IngresDriver
     * jdbc:h2 org.h2.Driver
     * jdbc:mckoi com.mckoi.JDBCDriver
     * jdbc:cloudscape COM.cloudscape.core.JDBCDriver
     * jdbc:informix-sqli com.informix.jdbc.IfxDriver
     * jdbc:timesten com.timesten.jdbc.TimesTenDriver
     * jdbc:as400 com.ibm.as400.access.AS400JDBCDriver
     * jdbc:sapdb com.sap.dbtech.jdbc.DriverSapDB
     * jdbc:JSQLConnect com.jnetdirect.jsql.JSQLDriver
     * jdbc:JTurbo com.newatlanta.jturbo.driver.Driver
     * jdbc:firebirdsql org.firebirdsql.jdbc.FBDriver
     * jdbc:interbase interbase.interclient.Driver
     * jdbc:pointbase com.pointbase.jdbc.jdbcUniversalDriver
     * jdbc:edbc ca.edbc.jdbc.EdbcDriver
     * jdbc:mimer:multi1 com.mimer.jdbc.Driver
     * @return
     */
    DBDialect parse() {
        Assert.notNull(url);
        int start = url.indexOf("jdbc:");
        if (start == -1) {
            throw new IllegalArgumentException("The jdbc url [" + url + "] cann't be recognized.");
        }
        int end = url.indexOf(':', start + 5);
        String dbType = url.substring(start + 5, end);
        profile = DBDialect.getDialect(dbType); //传入时会自动转为小写
        if (profile == null) {
            throw new PersistenceException("database not supported:" + dbType);
        }
        if (url.length() > 0) {
//			profile.parseDbInfo(this);
        }
        return profile;
    }
}