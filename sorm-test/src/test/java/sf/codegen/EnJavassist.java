package sf.codegen;

import org.junit.Test;
import org.sf.mavenplugin.goal.EnhanceMojo;

public class EnJavassist {
    public static void main(String[] args) throws Exception {
        new EntityEnhancerJavassist().enhance("sf.db.domain");
    }
    @Test
    public  void t1(){
        EnhanceMojo em = new EnhanceMojo();
        em.setPath("E:/SoftwareProject/STSMyProject/SormGit/sorm-test/target/classes");
        em.execute();
    }
}
