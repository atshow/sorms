package jetbrick.template;

import org.junit.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import sf.database.DBObject;
import db.domain.User;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

public class BeanTest {

    public static Map<String, Object> BeanMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        BeanWrapper beanWrapper = new BeanWrapperImpl(obj);
        PropertyDescriptor[] descriptor = beanWrapper.getPropertyDescriptors();
        for (int i = 0; i < descriptor.length; i++) {
            String name = descriptor[i].getName();
            map.put(name, beanWrapper.getPropertyValue(name));
        }
        return map;
    }

    @Test
    public void t() {
        User u = new User();
        u.setId(12312L);
        for(int i=0;i<10000;i++){
            BeanMap(u);
        }
    }

    public static void main(String[] args) {
        System.out.println(DBObject.class.getName());
    }
}
