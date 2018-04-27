package jetx;

import org.junit.Test;
import db.domain.User;
import sf.tools.reflect.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class A {
    public static void main(String[] args) {
        User u = new User();
        u.setNicename("123");
        String name = (String) BeanUtils.getFieldValue(u, "nicename");
        System.out.println(name);
    }

    @Test
    public void test2(){

        List<String> list = new ArrayList<>();
        for(int i=0;i<10000;i++)
            list.add(String.valueOf(i));
        //lambda表达式
        long start = System.currentTimeMillis();
//        list.parallelStream().forEach((s)->{
//            s.toString();
//        });
        //普通测试
        for (String s :list){
            s.length();
        }
        long end = System.currentTimeMillis();
        System.out.println("耗时："+(end-start) +"  ms");
    }

    @Test
    public void test3(){

        List<String> list = new ArrayList<>();
        for(int i=0;i<10000;i++)
            list.add(String.valueOf(i));
        //lambda表达式
        long start = System.currentTimeMillis();
        list.stream().forEach((s)->{
            s.length();
        });
        //普通测试

        long end = System.currentTimeMillis();
        System.out.println("耗时："+(end-start) +"  ms");
    }
}
