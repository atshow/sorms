package sf.codegen;

import java.net.URL;

/*
 *
 * @author
 * @Date
 */
public interface IEntityEnhancer {
    IEntityEnhancer addRoot(URL url);

    void enhance(String... pkgNames);
}
