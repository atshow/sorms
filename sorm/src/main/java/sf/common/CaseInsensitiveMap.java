package sf.common;

import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 用于存储结果的Map
 * 其中key是大小写不敏感的
 * @see org.springframework.util.LinkedCaseInsensitiveMap
 */
public class CaseInsensitiveMap<V> implements Map<String, V>, Cloneable, Serializable {
    private static final long serialVersionUID = 2228797892548164705L;
    private final LinkedHashMap<String, V> map = new LinkedHashMap<String, V>();

    public CaseInsensitiveMap() {
    }

    public CaseInsensitiveMap(Map<String, V> m) {
        super();
        putAll(m);
    }


    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        if (key instanceof String) {
            return map.get(((String) key).toLowerCase());
        }
        return map.get(key);
    }

    @Override
    public V put(String key, V value) {
        return map.put(key == null ? key : key.toLowerCase(), value);
    }

    public V getLowerKey(String key) {
        return map.get(key);
    }

    public V putLowerKey(String key, V value) {
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, V>> entrySet() {
        return map.entrySet();
    }

    public static CaseInsensitiveMap<Object> createFromBean(Object bean) {
        CaseInsensitiveMap<Object> v = new CaseInsensitiveMap<Object>();

        BeanWrapperImpl bw = new BeanWrapperImpl(bean);
        for (PropertyDescriptor pd : bw.getPropertyDescriptors()) {
            String name = pd.getName();
            v.put(name, pd.getValue(name));
        }
        return v;
    }


    @Override
    public CaseInsensitiveMap<V> clone() {
        return new CaseInsensitiveMap<>(this);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return map.equals(obj);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
