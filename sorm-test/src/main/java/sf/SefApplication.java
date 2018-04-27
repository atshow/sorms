package sf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sf.codegen.EntityEnhancerJavassist;

@SpringBootApplication
public class SefApplication {
	public static void main(String[] args) throws Exception {
		new EntityEnhancerJavassist().enhance("db.domain");
		SpringApplication.run(SefApplication.class, args);
	}

}
