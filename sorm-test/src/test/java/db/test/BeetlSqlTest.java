package db.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.StringTemplateResourceLoader;
import org.beetl.sql.core.*;
import org.beetl.sql.core.db.DBStyle;
import org.beetl.sql.core.db.MySqlStyle;
import org.beetl.sql.ext.DebugInterceptor;
import org.junit.Test;

import db.domain.User;

public class BeetlSqlTest {
	// @Test
	public void test() {
		ConnectionSource source = ConnectionSourceHelper.getSimple("com.mysql.jdbc.Driver",
				"jdbc:mysql://localhost:3306/wordpress", "root", "123456");
		DBStyle mysql = new MySqlStyle();

		SQLLoader loader = new ClasspathLoader("/sql");

		DefaultNameConversion nc = new DefaultNameConversion();

		SQLManager sqlManager = new SQLManager(mysql, loader, source, nc, new Interceptor[] { new DebugInterceptor() });

		User query2 = new User();
		query2.setLoginName("xiandafu");
		List<User> list2 = sqlManager.select("user.select", User.class, query2);

		User user = new User();
		user.setLoginName("");
		user.setNicename("xiandafu");

		int id = 1;
		user = sqlManager.unique(User.class, id);

		User newUser = new User();
		newUser.setId(1l);
		newUser.setLock(false);
		sqlManager.updateTemplateById(newUser);

		User query = new User();
		query.setLoginName("xiandafu");
		List<User> list = sqlManager.template(query);

	}

	@Test
	public void test1() throws IOException {
		StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader();
		Configuration cfg = Configuration.defaultConfiguration();
		GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
		Template t = gt.getTemplate("hello,${name}");
		t.binding("name", "beetl");
		String str = t.render();
		System.out.println(str);

		Map<String, Object> paras = new HashMap<>();
		paras.put("age", 12);

		run(paras, null);
	}

	protected SQLResult run(Map<String, Object> paras, String parentId) throws IOException {
		StringBuilder sb = new StringBuilder();
		// sb.append("select \n");
		// sb.append("===\n");
		sb.append("select * from user where 1=1\n");
		sb.append("@if(!isEmpty(age)){\n");
		sb.append("and age = :{age/3}\n");
		sb.append("@}\n");
		sb.append("@if(!isEmpty(name)){\n");
		sb.append("and name = :{name}\n");
		sb.append("@}");
		SQLResult result = null;
//		result = BeetlSqlHelp.getSQLResultById(paras, "select");
		return result;
	}
}
