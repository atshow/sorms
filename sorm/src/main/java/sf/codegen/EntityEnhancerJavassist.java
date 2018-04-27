package sf.codegen;

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.smallframework.spring.path.SpringClassPathScanning;
import org.springframework.core.io.Resource;
import org.springframework.core.type.ClassMetadata;
import sf.common.log.LogUtil;
import sf.tools.IOUtils;

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
public class EntityEnhancerJavassist implements IEntityEnhancer {

    PrintStream out = System.out;
    private List<URL> roots;


    public void setOut(PrintStream out) {
        this.out = out;
    }

    @Override
    public IEntityEnhancer addRoot(URL url) {
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
     * @param pkgNames
     */
    @Override
    public void enhance(final String... pkgNames) {
        ClassPool pool = ClassPool.getDefault();
        int n = 0;
        if (roots == null || roots.size() == 0) {
            Resource[] resources = SpringClassPathScanning.getClassResources(pkgNames);
            if (resources != null) {
                for (Resource r : resources) {
                    ClassMetadata metadata = SpringClassPathScanning.getClassMetadata(r);
                    String cls = metadata.getClassName();
                    try {
                        if (enhanceJavassist(cls, r, pool)) {
                            n++;
                        }
                    } catch (Exception e) {
                        LogUtil.exception(e);
                        LogUtil.error("Enhance error: " + cls + ": " + e.getMessage());
                        continue;
                    }
                }
            }
        } else {
            for (URL root : roots) {
                //设置类加载路径
                ClassPath cp = null;
                try {
                    cp = pool.appendClassPath(root.getPath());
                } catch (NotFoundException e) {
                    throw new RuntimeException(e);
                }
                Resource[] resources = SpringClassPathScanning.getClassResources(root, pkgNames);
                if (resources != null) {
                    for (Resource r : resources) {
                        ClassMetadata metadata = SpringClassPathScanning.getClassMetadata(r);
                        String cls = metadata.getClassName();
                        try {
                            if (enhanceJavassist(cls, r, pool)) {
                                n++;
                            }
                        } catch (Exception e) {
                            LogUtil.error("Enhance error: {}", cls, e);
                            continue;
                        }
                    }
                }
                //回收路径资源.
                if (cp != null) {
                    pool.removeClassPath(cp);
                }
            }
        }
        out.println(n + " classes enhanced.");
    }

    private boolean enhanceJavassist(String cls, Resource resource, ClassPool pool) throws IOException, Exception {
        EnhanceTaskJavassist enhancer = new EnhanceTaskJavassist();
        byte[] result = enhancer.doEnhance(pool, cls);
        if (result != null) {
            if (result.length == 0) {
                out.println(cls + " is already enhanced.");
            } else {
                IOUtils.saveAsFile(resource.getFile(), result);
                out.println("enhanced class:" + cls);// 增强完成
                return true;
            }
        }
        return false;
    }

}
