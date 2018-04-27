package spring.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import db.domain.Role;
import db.domain.User;
import db.domain.User.CascadeField;
import db.domain.UserMeta;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smallframework.spring.DaoTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sf.SefApplication;
import sf.codegen.EntityEnhancerJavassist;
import sf.common.wrapper.Page;
import sf.database.template.jetbrick.JetbrickHandler;
import sf.ext.gen.GenConfig;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "application.properties" }, classes = {
		SefApplication.class }, webEnvironment = WebEnvironment.NONE)
@Transactional
@Rollback
public class UserDAO {
	static {
		// new EntityEnhancer().enhanceJavassist("sf.db.domain");
        just();
	}

//	@BeforeClass
    public static void just(){
        new EntityEnhancerJavassist().enhance("db.domain");
    }

	static Logger log = LoggerFactory.getLogger(UserDAO.class);

	@Resource
	private DaoTemplate dt;

	@BeforeClass
	public static void before() {
		JetbrickHandler.getInstance().loadAllSQL();
	}

	@Test
	public void testDB1() {
		String sql = "select * from wp_users";
		List<User> list = dt.selectList(User.class, sql);
		System.out.println(list.size());

		Page<User> page = dt.selectPage(0, 10, User.class, sql);
		User u = dt.selectOne(User.class, sql);

		long start = System.currentTimeMillis();
		List<User> list2 = dt.selectList(User.class, "select * from wp_users where login_name like concat('%',?,'%')",
				"%u");
		System.out.println(System.currentTimeMillis() - start + "ms");
		System.out.println(list.size());

	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void testInsert() {
		User user = dt.selectOne(new User());
		User u = new User();
		u.setLoginName(UUID.randomUUID().toString());
		u.setDeleted(false);
		u.setCreated(new Date());
		u.setActivationKey("23k4j2k3j4i234j23j4");
		int i = dt.insert(u);
		dt.update(u);
		// dt.delete(u);
		System.out.println(u.getId());

	}

	@Test
	public void testInsertCacsde() {
		User u = new User();
		u.setLoginName(UUID.randomUUID().toString());
		u.setDeleted(false);
		u.setActivationKey("23k4j2k3j4i234j23j4");
		UserMeta um = new UserMeta();
		um.setKey("滚");
		um.setValue("滚滚");

		log.info("普通级联");
		int i = dt.insertCascade(u);
		u.setLoginName("sdfer");
		um.setKey("haha");
		dt.updateCascade(u);
		dt.deleteCascade(u);

		log.info("指定字段级联");
		u = new User();
		u.setLoginName(UUID.randomUUID().toString());
		u.setDeleted(false);
		u.setActivationKey("23k4j2k3j4i234j23j4");
		um = new UserMeta();
		um.setKey("滚");
		um.setValue("滚滚");
		u.setUserMetaSet(Sets.newHashSet(um));

		i = dt.insertCascade(u, CascadeField.userMetaSet);
		u.setLoginName("sdfer");
		um.setKey("haha");
		dt.updateCascade(u, CascadeField.userMetaSet);
		dt.deleteCascade(u, CascadeField.userMetaSet);
		// dt.delete(u);
		System.out.println(u.getId());
	}

	@Test
	public void testBashInsert() {
		User u = new User();
		u.setDeleted(false);
		u.setLoginName("admin");
		u.setActivationKey("1");
		User u2 = new User();
		u2.setDeleted(true);
		u2.setLoginName("test");
		u2.setActivationKey("1");
		List<User> uList = new ArrayList<>();
		uList.add(u);
		uList.add(u2);
		int[] i = dt.batchInsert(uList);

		u.setDeleted(true);
		u2.setDeleted(false);
		dt.batchUpdate(uList);

		// dt.batchDelete(uList);

		System.out.println("批量插入:");
		System.out.println(u.getId() + " " + i[0]);
		System.out.println(u2.getId() + " " + i[1]);
	}

	@Test
	public void testBashInsert2() {
		String sql = "select * from wp_users";
		List<User> lists = dt.selectList(User.class, sql);
		System.out.println(lists.size());

		List<User> list = new ArrayList<>();
		for (int i = 0; i < 10000; i++) {
			User u = new User();
			u.setDeleted(false);
			u.setLoginName(UUID.randomUUID().toString());
			u.setActivationKey("1");
			list.add(u);
		}
		dt.batchInsertFast(list);
		System.out.println(list.size());
	}

	@Test
	public void testTemplate() {
		Map<String, Object> query2 = new HashMap<>();
		query2.put("id", 1);
		List<User> list2 = dt.selectListTemplate(User.class, "queryUserByName2", query2);
		long start = System.currentTimeMillis();
		list2 = dt.selectListTemplate(User.class, "queryUserByName2", query2);
		System.out.println(System.currentTimeMillis() - start + "ms -->1");
		for (User u : list2) {
			dt.fetchLinks(u);
		}
		// System.out.println(list2);
	}

	@Test
	public void testLinks() {
		// dt.createTable(Role.class);
		// dt.createTable(UserRole.class);
		// dt.createTable(UserMeta.class);
		User q = new User();
		q.setId(1L);
		User user = dt.selectOne(q);
		if (user != null) {
			user = dt.fetchLinks(user);
		}
		UserMeta um = dt.selectOne(new UserMeta());
		if (um != null) {
			dt.fetchLinks(um);
		}
		Role r = dt.selectOne(new Role());
		if (r != null) {
			dt.fetchLinks(r);
		}
		System.out.println(1);
	}

	@Test
	// @Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void testRelation() {
		// dt.createTable(Role.class);
		// dt.createTable(UserRole.class);
		// dt.createTable(UserMeta.class);
		User user = dt.selectByPrimaryKeys(User.class, 7);
		List<Role> roles = dt.selectList(new Role());
		if (user != null) {
			user.setRoles(roles);
			dt.updateRelation(user, CascadeField.roles);
		}
		System.out.println(1);
	}

	@Test
	// @Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void testMoreDataSource() {
		// dt.createTable(Role.class);
		// dt.createTable(UserRole.class);
		// dt.createTable(UserMeta.class);
		User user = dt.selectByPrimaryKeys(User.class, 7);
		// Privilege pri = dt.useContext("sqlite").selectOne(Privilege.class,
		// "select * from privilege limit 1");
		// User user = dt.selectByPrimaryKeys(User.class, 7);
		System.out.println(user);
	}

	@Test
	// @Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void testLike() {
		// dt.createTable(Role.class);
		// dt.createTable(UserRole.class);
		// dt.createTable(UserMeta.class);
		User user = dt.selectByPrimaryKeys(User.class, 7);
		List<Role> roles = dt.selectList(new Role());
		if (user != null && !roles.isEmpty()) {
			user.setRoles(roles);
			dt.updateRelation(user, CascadeField.roles);
		}
		System.out.println(1);
	}

	@Test
	// @Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void testSelect() throws JsonProcessingException, InterruptedException {
		// dt.createTable(Role.class);
		// dt.createTable(UserRole.class);
		// dt.createTable(UserMeta.class);
		User q = new User();
		q.setId(1L);
		User user = dt.selectOne(q);
		System.out.println("FastJson:"
				+ JSON.toJSONString(user, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty));
		ObjectMapper om = new ObjectMapper();
		om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		System.out.println("JackSon:" + om.writeValueAsString(user));
		List<Role> roles = dt.selectList(new Role());
		if (user != null && !roles.isEmpty()) {
			user.setRoles(roles);
			dt.updateRelation(user, CascadeField.roles);
		}
		System.out.println(1);
	}

	@Test
	// @Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void testLambda() {
		User q = new User();
		dt.selectStream((u) -> {
			u.map(a -> a.getId()).collect(Collectors.toList());
		}, q);
		long start = System.currentTimeMillis();
		dt.selectIterator(it -> {
			it.forEachRemaining(user -> {
				user.getId();
			});
		}, q);
		System.out.println(System.currentTimeMillis() - start + "ms");
	}

	@Test
//	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void testOLock() {
		String sql = "select * from wp_users";
		List<User> lists = dt.selectList(User.class, sql);
		System.out.println(lists.size());

		UserMeta um = new UserMeta();
		um.setKey("we");
		um.setValue("erer");
		dt.insert(um);

		dt.setNewOptimisticLockValues(um);
		um.setValue("okmiyou");
//		um.setVersion2(um.getVersion2());
//		um.setVersion(um.getVersion());
//		um.setVersion3(um.getVersion3());
		int i =dt.updateAndSet(um);
		System.out.println("update count:"+i);
	}

	@Test
	public void testGenCodes() {
		GenConfig genConfig =new GenConfig();
		dt.genPojoCodes("target","com/test/abc",genConfig);

		System.out.println("update count:");
	}
}
