package db.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class JetbrickSqlTest {

    public URL getFile(String filePath) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = null;
//		InputStream is  = null;
        if (loader != null) {
            url = loader.getResource(filePath);
            if (url != null) {
                return url;
            } else {
                url = this.getClass().getResource(filePath);
                return url;
            }
        } else {
            url = this.getClass().getResource(filePath);
            return url;
        }
    }

    private List<String> readSqlFile( URL url) {
        List<String> list = new LinkedList<String>();
        if (url == null) {
            return list;
        }
        InputStream ins;
        try {
            ins = url.openStream();
        } catch (IOException e1) {
            return list;
        }

        BufferedReader bf = null;
        try {

            bf = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
            String line =null;
            while ((line = bf.readLine()) != null) {
               list.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public static void main(String[] args) {
        JetbrickSqlTest test = new JetbrickSqlTest();
        URL url = test.getFile("/jetx.sql");
        List<String> list = test.readSqlFile(url);
        System.out.println(url);
    }
}
