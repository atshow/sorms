package sf.tools.resource;

import sf.tools.IOUtils;
import sf.tools.IOUtils.FileFilterEx;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;


/**
 * 通过本地文件查找实现的资源定位
 * @author jiyi
 */
public class FileSearchLoader extends AResourceLoader {
    private List<File> roots;

    public FileSearchLoader(File... root) {
        this.roots = Arrays.asList(root);
    }

    public URL getResource(String name) {
        name = name.replace('\\', '/');
        if (name.endsWith("/")) name = name.substring(0, name.length() - 1);
        final String key = name;
        for (File root : roots) {
            File file = IOUtils.findFile(root, new FileFilterEx() {
                public boolean accept(File pathname) {
                    try {
                        String path = pathname.getCanonicalPath().replace('\\', '/');
                        return path.endsWith(key);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            });
            if (file != null)
                return toURL(file);
        }
        return null;
    }

    public List<URL> getResources(String name) {
        name = name.replace('\\', '/');
        if (name.endsWith("/")) name = name.substring(0, name.length() - 1);
        final String key = name;
        Set<URL> result = new LinkedHashSet<URL>();
        for (File root : roots) {
            Collection<File> found = IOUtils.findFiles(root, new FileFilterEx() {
                public boolean accept(File pathname) {
                    try {
                        String path = pathname.getCanonicalPath().replace('\\', '/');
                        return path.endsWith(key);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            });
            for (File f : found) {
                result.add(toURL(f));
            }
        }
        return Arrays.asList(result.toArray(new URL[result.size()]));
    }
}
