package sf.controller;

import javax.annotation.Resource;

import org.smallframework.spring.DaoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import sf.common.wrapper.Page;
import db.domain.User;
import db.domain.User.Names;

@Controller
@Transactional
public class Main {
	@Resource
	private DaoTemplate dt;
	@Resource
	private JdbcTemplate jt;

	@RequestMapping("/index")
	@ResponseBody
	public ModelMap index() {
		ModelMap mm = new ModelMap();
		User u = new User();
		u.setDeleted(false);
		u.setDisplayName(Names.lisi);
		u.setActivationKey("23k4j2k3j4i234j23j4");
		int i = dt.insert(u);

		Page<User> page = dt.selectPage(0, 1, User.class, "select * from wp_users");

		System.out.println(page);

		return mm;
	}

	@RequestMapping("/indexjt")
	@ResponseBody
	public ModelMap indexJt() {
		ModelMap mm = new ModelMap();
		jt.execute("INSERT INTO wp_users(activation_key)VALUES('1111')");
		jt.queryForList("select * from wp_users");
		return mm;
	}
}
