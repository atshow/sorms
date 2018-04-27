package db.test;

import org.objectweb.asm.util.ASMifier;

public class ASMTest {
	public static void main(String[] args) throws Exception {
		ASMifier.main(new String[] { "sf.db.domain.User" });
		// Textifier.main(new String[]{"sf.db.domain.User"});
	}
}
