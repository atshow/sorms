package db.test;

import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;
import db.domain.User;

public class Met {
	public static void main(String[] args) {
		TableMapping tb = MetaHolder.getMeta(User.class);
		System.out.println(tb);

		String str = "qqqq";
		String[] arr = str.split("---");
		System.out.println(arr.length);

	}
}
