package sf.database.mapper;

/**
 * 定义获取Mapper实现的接口,这样可以很容易的更换实现.
 * @author zhoupan.
 */
public interface MapperBuilder {

    /**
     * Gets the dao2.
     * @param <T>             the generic type
     * @param mapperInterface the dao2 interface
     * @return the dao2
     */
    <T> T getMapper(Class<T> mapperInterface);

}