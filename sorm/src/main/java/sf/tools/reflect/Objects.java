package sf.tools.reflect;

import java.io.*;

public class Objects {
    /**
     * 深拷贝
     * @param s
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <S extends Serializable> S deepCopy(S s) {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(s);
            ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            return (S) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (oos != null) {
                    oos.close();
                }
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
