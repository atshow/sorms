package db.test;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

public class JsonTest {
    @Test
    public void json(){
        System.out.println(JSON.toJSON(1));
    }
}
