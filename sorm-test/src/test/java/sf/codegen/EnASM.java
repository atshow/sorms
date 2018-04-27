package sf.codegen;

public class EnASM {

	public static void main(String[] args) throws Exception {
		new EntityEnhancerASM().enhance("sf.db.domain");
	}
}
