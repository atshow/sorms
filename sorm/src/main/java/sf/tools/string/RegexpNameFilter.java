package sf.tools.string;

import sf.tools.NameFilter;

/**
 * 使用正则表达式的名称过滤器
 * @author Administrator
 */
public class RegexpNameFilter implements NameFilter {
    private String includePattern;
    private String[] excludePatter;

    public RegexpNameFilter() {
    }

    ;

    /**
     * 构造
     * @param include 要包含的正则表达式
     * @param exclude 要排除的正则表达式
     */
    public RegexpNameFilter(String include, String... exclude) {
        this.includePattern = include;
        this.excludePatter = exclude;
    }

    /**
     * 计算传入的文件名是否匹配正则表达式模板
     * @param 传入文件名
     * @return 如果指定了包含正则表达式，不匹配的话就返回false;
     * <p>
     * 如果指定了排除正则表达式，并匹配了的话，返回false。
     * <p>
     * 剩余情况都返回true
     */
    public boolean accept(String name) {
        if (this.includePattern != null) {
            if (!RegexpUtils.matches(name, includePattern)) {
                return false;
            }
        }
        if (this.excludePatter != null) {
            for (String sp : excludePatter) {
                if (RegexpUtils.matches(name, sp)) {
                    return false;
                }
            }
        }
        return true;
    }
}
