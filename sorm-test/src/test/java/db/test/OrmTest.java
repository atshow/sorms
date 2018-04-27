package db.test;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import sf.codegen.EntityEnhancerASM;
import sf.database.jdbc.sql.Crud;
import db.domain.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

public class OrmTest {
	public Connection conn;

	public static void main(String[] args) {
		// new EntityEnhancer().enhanceJavassist("sf.db.domain");
		new EntityEnhancerASM().enhance("sf.db.domain");
	}

	@BeforeClass
	public static void all() {
		// new EntityEnhancer().enhanceASM("sf.db.domain");
	}

	@Before
	public void init() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/wordpress", "root", "123456");
	}

	// @Test
	public void sql() throws Exception {
		Crud client = Crud.getInstance();
		conn.setAutoCommit(false);
		User u = new User();
		u.setLoginName("test");
		u.setPassword("1587");
		u.setRegistered(new Date());
		u.setDisplayName(User.Names.zhangshang);
		client.getCrudModel().insert(conn, u);
		System.out.println(u.getId());
		// conn.rollback();
		conn.commit();
		conn.close();
	}

	@Test
	public void query() throws Exception {
		Crud client = Crud.getInstance();
		conn.setAutoCommit(false);
		User u = new User();
		u.setLoginName("test");
		u.setPassword("1587");
		u.setRegistered(new Date());
		u.setDisplayName(User.Names.zhangshang);
		List<User> list = client.getCrudSql().selectList(conn, User.class, "select * from wp_users");
		System.out.println(list.size());
		// conn.rollback();
		conn.commit();
		conn.close();
	}

	@Test
	public void queryFromFile() throws Exception {
		Crud client = Crud.getInstance();
		conn.setAutoCommit(false);
		User u = new User();
		u.setLoginName("test");
		u.setPassword("1587");
		u.setRegistered(new Date());
		u.setDisplayName(User.Names.zhangshang);

		Map<String, Object> context = new HashMap<>();
		context.put("loginName", "admin");
		context.put("nicename", "admin");
		context.put("username","admin");
		context.put("nicenames", Arrays.asList("admin","123"));
		client.getCrudTemplate().select(conn, "queryUserByName", context);

		List<User> list = client.getCrudTemplate().selectList(conn, User.class, "queryUserByName", context);
		System.out.println(list.size());
		// conn.rollback();
		conn.commit();
		conn.close();
	}

}
