package sf.codegen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smallframework.spring.path.SpringClassPathScanning;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.Resource;
import sf.tools.IOUtils;
import sf.tools.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity静态增强任务类
 * <h3>作用</h3> 这个类中提供了{@link #enhance(String...)}
 * 方法，可以对当前classpath下的Entity类进行字节码增强。
 * @author
 * @Date
 */
public class EntityEnhancerASM implements IEntityEnhancer {
    private String includePattern;
    private String[] excludePatter;
    private List<URL> roots;
    PrintStream out = System.out;
    private EnhanceTaskASM enhancer;
    private static final Logger log = LoggerFactory.getLogger(EntityEnhancerASM.class);

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public EntityEnhancerASM() {
        enhancer = new EnhanceTaskASM(new ClassRelativeResourceLoader(this.getClass()));
    }

    @Override
    public EntityEnhancerASM addRoot(URL url) {
        if (url != null) {
            if (roots == null) {
                roots = new ArrayList<URL>();
            }
            roots.add(url);
        }
        return this;
    }


    /**
     * 在当前的classpath目录下扫描Entity类(.clsss文件)，使用字节码增强修改这些class文件。
     * @param pkgNames 要增强的包名
     */
    @Override
    public void enhance(final String... pkgNames) {
        int n = 0;
        if (roots == null || roots.size() == 0) {
            Resource[] resources = SpringClassPathScanning.getClassResources(pkgNames);
            for (Resource cls : resources) {
                if (cls.isFile()) {
                    try {
                        if (processEnhance(cls)) {
                            n++;
                        }
                    } catch (Exception e) {
                        log.error("Enhance error: {}", cls, e);
                        continue;
                    }
                }
            }
        } else {
            for (URL root : roots) {
                Resource[] clss = SpringClassPathScanning.getClassResources(root, pkgNames);
                for (Resource cls : clss) {
                    if (!cls.isFile()) {
                        continue;
                    }
                    try {
                        if (processEnhance(cls)) {
                            n++;
                        }
                    } catch (Exception e) {
                        log.error("Enhance error: {}", cls, e);
                        continue;
                    }
                }
            }
        }
        out.println(n + " classes enhanced.");
    }

    /**
     * 增强制定名称的类
     * @param className 类全名
     * @return 是否进行增强
     */
    public boolean enhanceClass(String className) {
        URL url = this.getClass().getClassLoader().getResource(className.replace('.', '/') + ".class");
        if (url == null) {
            throw new IllegalArgumentException("not found " + className);
        }
        try {
            return enhance(IOUtils.urlToFile(url), className);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean enhance(File f, String cls) throws IOException, Exception {
        EnhanceTaskASM enhancer = new EnhanceTaskASM();
        File sub = new File(f.getParentFile(), StringUtils.substringAfterLastIfExist(cls, ".").concat("$Field.class"));
        byte[] result = enhancer.doEnhance(IOUtils.toByteArray(f), (sub.exists() ? IOUtils.toByteArray(sub) : null));
        if (result != null) {
            if (result.length == 0) {
                out.println(cls + " is already enhanced.");
            } else {
                IOUtils.saveAsFile(f, result);
                out.println("enhanced class:" + cls);// 增强完成
                return true;
            }
        }
        return false;
    }


    private boolean processEnhance(Resource cls) throws Exception {
        File f = cls.getFile();
        File sub = new File(IOUtils.removeExt(f.getAbsolutePath()).concat("$Field.class"));
        if (!f.exists()) {
            return false;
        }
        byte[] result = enhancer.doEnhance(IOUtils.toByteArray(f), (sub.exists() ? IOUtils.toByteArray(sub) : null));
        if (result != null) {
            if (result.length == 0) {
                out.println(cls + " is already enhanced.");
            } else {
                IOUtils.saveAsFile(f, result);
                out.println("enhanced class:" + cls);// 增强完成
                return true;
            }
        }
        return false;
    }

    /**
     * 设置类名Pattern
     * @return
     */
    public String getIncludePattern() {
        return includePattern;
    }

    public EntityEnhancerASM setIncludePattern(String includePattern) {
        this.includePattern = includePattern;
        return this;
    }

    public String[] getExcludePatter() {
        return excludePatter;
    }

    public EntityEnhancerASM setExcludePatter(String[] excludePatter) {
        this.excludePatter = excludePatter;
        return this;
    }
}
