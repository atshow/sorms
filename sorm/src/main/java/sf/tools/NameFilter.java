package sf.tools;

/**
 * 通用过滤器，检查一个String是否可以接受
 * @author Administrator
 */
public interface NameFilter {

    /**
     * @param name the string to check.
     * @return true if the string is acceptable.
     */
    public boolean accept(String name);
}
