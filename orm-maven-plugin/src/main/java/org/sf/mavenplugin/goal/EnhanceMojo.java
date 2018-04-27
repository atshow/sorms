package org.sf.mavenplugin.goal;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import sf.codegen.EntityEnhancerASM;
import sf.codegen.IEntityEnhancer;

import java.io.File;
import java.net.URL;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "enhance", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class EnhanceMojo extends AbstractMojo {
    /**
     * @parameter property="${enhance.path}"
     */
    @Parameter(defaultValue = "${enhance.path}")
    private String path;

    /**
     * The directory containing generated classes.
     *
     * @parameter property="${project.build.outputDirectory}"
     * @required
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private File classesDirectory;

    /**
     * 基路径
     *
     * @parameter property="${basedir}"
     * @required
     * @readonly
     */
    @Parameter(defaultValue = "${basedir}")
    protected String basedir;

    public void setPath(String path) {
        this.path = path;
    }

    public void execute() {
        long time = System.currentTimeMillis();
        try {
            this.getLog().info("Ormbuilder enhanceing entity classes......");

            String workPath;
            if (path == null) {
                workPath = classesDirectory.getAbsolutePath();
            } else {
                workPath = path;
            }
            workPath = workPath.replace('\\', '/');
            if (!workPath.endsWith("/")) {
                workPath = workPath + "/";
            }
            this.getLog().info("Ormbuilder enhance entity classes working path is: " + workPath);

            IEntityEnhancer en = new EntityEnhancerASM().addRoot(new URL("file://" + workPath));
            en.enhance();

            this.getLog().info("Ormbuilder enhance entity classes total use " + (System.currentTimeMillis() - time) + "ms");
        } catch (Exception e) {
            this.getLog().error(e);
        }
    }

    // TEST
    public static void main(String[] args) {
        String workPath = "E:/Git/ef-orm/orm-test/target/test-classes";
        EnhanceMojo em = new EnhanceMojo();
        em.setPath(workPath);
        em.execute();
    }

    public String getPath() {
        return path;
    }

    public File getClassesDirectory() {
        return classesDirectory;
    }

    public String getBasedir() {
        return basedir;
    }

}
