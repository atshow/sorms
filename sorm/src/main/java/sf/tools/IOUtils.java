package sf.tools;

import sf.common.log.LogUtil;
import sf.tools.io.UnicodeReader;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

public class IOUtils {
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final File[] EMPTY = new File[0];

    /**
     * 关闭指定的对象，不会抛出异常
     * @param input 需要关闭的资源
     */
    public static void closeQuietly(Closeable input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                LogUtil.exception(e);
            }
        }
    }

    /**
     * 将URL转化为文件
     * @param url 要转换的URL，必须是file://协议，否则抛出异常。
     */
    public static File urlToFile(URL url) {
        if (url == null)
            return null;
        try {
            URLFile file = new URLFile(url);
            if (file.isLocalFile())
                return file.getLocalFile();
            return file;
        } catch (RuntimeException e) {
            LogUtil.error(url.toString() + " is not a valid file:"
                    + e.getMessage());
            return null;
        }
    }

    /**
     * 将多个URL转换为多个文件对象
     * @param url
     * @return
     */
    public static File[] urlToFile(URL[] url) {
        File[] result = new File[url.length];
        for (int i = 0; i < url.length; i++) {
            result[i] = urlToFile(url[i]);
        }
        return result;
    }

    /**
     * 将指定的流保存为临时文件
     * @param is
     * @return
     * @throws IOException
     */
    public static File saveAsTempFile(InputStream is) throws IOException {
        File f = File.createTempFile("~tmp", ".io");
        saveAsFile(f, is);
        return f;
    }

    /**
     * 将输入流保存为文件
     * @param is
     * @param file
     * @throws IOException
     */
    public static void saveAsFile(File file, InputStream... iss)
            throws IOException {
        ensureParentFolder(file);
        BufferedOutputStream os = new BufferedOutputStream(
                new FileOutputStream(file));
        try {
            for (InputStream is : iss) {
                copy(is, os, false);
            }
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
    }

    /**
     * 检查/创建文件在所的文件夹
     * @param file
     */
    public static void ensureParentFolder(File file) {
        File f = file.getParentFile();
        if (f != null && !f.exists()) {
            f.mkdirs();
        } else if (f != null && f.isFile()) {
            throw new RuntimeException(f.getAbsolutePath()
                    + " is a exist file, can't create directory.");
        }
    }

    /*
     * Copies the contents of the given {@link InputStream} to the given {@link
     * OutputStream}.
     *
     * @param pIn The input stream, which is being read. It is guaranteed, that
     * {@link InputStream#close()} is called on the stream.
     * 关于InputStram在何时关闭的问题，我一直认为应当是成对操作的（即在哪个方法中生成Stream，就要在使用完后关闭），
     * 因此不打算在这里使用close方法。 但是后来我又考虑到，InputStream在使用完后，其内部标记已经发生了变化，无法再次使用。
     * (reset方法的效果和实现有关，并不能保证回复到Stream使用前的状态。)
     * 因此考虑这里统一关闭以防止疏漏，外面再关一次也不会有问题(作为好习惯，还是应该成对打开和关闭)。
     *
     * @param pOut 输出流，可以为null,此时输入流中的相应数据将丢弃
     *
     * @param pClose True guarantees, that {@link OutputStream#close()} is
     * called on the stream. False indicates, that only {@link
     * OutputStream#flush()} should be called finally.
     *
     * @param pBuffer Temporary buffer, which is to be used for copying data.
     *
     * @return Number of bytes, which have been copied.
     *
     * @throws IOException An I/O error occurred.
     */
    private static long copy(InputStream in, OutputStream out, boolean inClose,
                             boolean outClose, byte[] pBuffer) throws IOException {
        if (in == null)
            throw new NullPointerException();
        long total = 0;
        try {
            int res;
            while ((res = in.read(pBuffer)) != -1) {
                if (out != null) {
                    out.write(pBuffer, 0, res);
                }
                total += res;
            }
            if (out != null)
                out.flush();
        } finally {
            if (outClose)
                closeQuietly(out);
            if (inClose)
                closeQuietly(in);
        }
        return total;
    }

    /*
     * 同上、READER和Writer之间的拷贝
     */
    private static long copy(Reader in, Writer out, boolean inClose,
                             boolean outClose, char[] pBuffer) throws IOException {
        if (in == null)
            throw new NullPointerException();
        long total = 0;
        try {
            int res;
            while ((res = in.read(pBuffer)) != -1) {
                if (out != null) {
                    out.write(pBuffer, 0, res);
                }
                total += res;
            }
            if (out != null)
                out.flush();
        } finally {
            if (outClose && out != null)
                closeQuietly(out);
            if (inClose)
                closeQuietly(in);
        }
        return total;
    }

    /**
     * 流之间拷贝
     * @param in       输入
     * @param out      输出
     * @param inClose  关闭输入流？
     * @param outClose 关闭输出流?
     * @return
     * @throws IOException
     */
    public static long copy(InputStream in, OutputStream out, boolean inClose,
                            boolean outClose) throws IOException {
        return copy(in, out, inClose, outClose, new byte[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * 流之间拷贝
     * @param in       输入
     * @param out      输出
     * @param inClose  关闭输入流
     * @param outClose 关闭输出流
     * @return
     * @throws IOException
     */
    public static long copy(Reader in, Writer out, boolean inClose,
                            boolean outClose) throws IOException {
        return copy(in, out, inClose, outClose, new char[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * 流之间拷贝
     * @param in     输入
     * @param out    输出
     * @param pClose 关闭输出流?
     * @return 拷贝长度
     * @throws IOException
     */
    public static long copy(Reader in, Writer out, boolean pClose)
            throws IOException {
        return copy(in, out, true, pClose, new char[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * 流之间拷贝
     * @param in             输入
     * @param out            输出
     * @param closeOutStream 关闭输出流? (输入流默认关闭)
     * @return
     * @throws IOException
     */
    public static long copy(InputStream in, OutputStream out,
                            boolean closeOutStream) throws IOException {
        return copy(in, out, true, closeOutStream,
                new byte[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * 将制定的URL中的数据读出成byte[]
     * @param url 资源目标位置
     * @return 字节数组
     * @throws IOException IO操作异常
     */
    public static byte[] toByteArray(URL url) throws IOException {
        return toByteArray(url.openStream());
    }

    /**
     * 读取文件到内存(不可用于大文件)
     * @param file 本地文件
     * @return 字节数组
     * @throws IOException IO操作异常
     */
    public static byte[] toByteArray(File file) throws IOException {
        InputStream in = (file instanceof URLFile) ? ((URLFile) file)
                .getInputStream() : new FileInputStream(file);
        try {
            byte[] result = toByteArray(in, (int) file.length());
            return result;
        } finally {
            in.close();
        }
    }

    /**
     * 读取文件到内存(不可用于大文件)
     * @param file
     * @return
     * @throws IOException
     * @deprecated use {@linkp #toByteArray(File)}
     */
    public static byte[] asByteArray(File file) throws IOException {
        return toByteArray(file);
    }

    /**
     * 读取流数据到内存。注意这个方法会将数据流全部读入到内存中，因此不适用于很大的数据对象
     * @param in
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream in) throws IOException {
        try {
            byte[] msg = toByteArray(in, -1);
            return msg;
        } finally {
            in.close();
        }
    }

    /**
     * 从流中读取指定的字节，第三个版本，性能再度提升 参考数据，从120M文件中读取前60M，此方法耗时125ms,v2耗时156ms
     * @param in
     * @param length 要读取的字节数，-1表示不限制。（注意实际处理中-1的情况下最多读取2G数据，超过2G不会读取）
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream in, int length)
            throws IOException {
        ByteArrayOutputStream out;
        if (length > 0) {
            out = new ByteArrayOutputStream(length);
        } else {
            out = new ByteArrayOutputStream(1024);
        }
        int buf = DEFAULT_BUFFER_SIZE;
        byte[] pBuffer = new byte[buf];
        int left = (length > 0) ? length : Integer.MAX_VALUE;// 剩余字节数
        while (left >= buf) {
            int n = in.read(pBuffer);
            if (n == -1) {
                left = 0;
                break;
            }
            left -= n;
            out.write(pBuffer, 0, n);
        }
        while (left > 0) {
            int n = in.read(pBuffer, 0, left);
            if (n == -1) {
                break;
            }
            left -= n;
            out.write(pBuffer, 0, n);
        }
        out.close();// ByteArrayOut其实是不需要close的，这里close是为了防止一些代码检查工具提出警告
        byte[] message = out.toByteArray();
        return message;
    }

    /**
     * 将内存数据块写入文件
     * @param file
     * @param data
     * @throws IOException
     */
    public static void saveAsFile(File file, byte[] data) throws IOException {
        saveAsFile(file, false, data);
    }

    /**
     * 将reader内容保存为文件
     * @param reader
     * @param file
     * @throws IOException
     */
    public static void saveAsFile(File file, Charset charset, Reader... readers)
            throws IOException {
        BufferedWriter os = getWriter(file,
                charset == null ? null : charset.name(), false);
        try {
            for (Reader reader : readers) {
                copy(reader, os, true, false, new char[2048]);
            }
        } finally {
            closeQuietly(os);
        }
    }

    /**
     * 将内存数据块写入文件
     * @param data
     * @param file
     * @throws IOException
     */
    public static void saveAsFile(File file, boolean append, byte[] data)
            throws IOException {
        ensureParentFolder(file);
        OutputStream out = new FileOutputStream(file, append);
        try {
            out.write(data);
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

    /**
     * 获得文本文件写入流
     * @param target
     * @param charSet
     * @param append
     * @return
     * @throws IOException
     */
    public static BufferedWriter getWriter(File target, String charSet,
                                           boolean append) {
        ensureParentFolder(target);
        try {
            OutputStream os = new FileOutputStream(target, append);
            if (charSet == null)
                charSet = Charset.defaultCharset().name();
            OutputStreamWriter osw = new OutputStreamWriter(os, charSet);
            return new BufferedWriter(osw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 从URL获得reader
     * @param file
     * @param charSet
     * @return
     * @throws IOException
     */
    public static BufferedReader getReader(URL file, String charSet) {
        if (file == null)
            return null;
        try {
            InputStream is = file.openStream();
            InputStreamReader isr = new InputStreamReader(is, charSet);
            return new BufferedReader(isr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获得一个供读取文本的reader, 本方法可以从BOM自动判断utf-8, unicode等类型，因此charset一般可以为null.
     * 只有当文件为不带BOM的unicode时才需要指定。
     * @param file
     * @param charSet
     * @return
     * @throws IOException
     */
    public static BufferedReader getReader(File file, String charSet) throws IOException {
        if (file == null)
            return null;
        InputStream is = (file instanceof URLFile) ? ((URLFile) file).getInputStream() : new FileInputStream(file);
        UnicodeReader isr = new UnicodeReader(is, charSet);
        return new BufferedReader(isr);
    }

    /**
     * 获得Reader
     * @param is
     * @param charSet
     * @return
     * @throws IOException
     */
    public static BufferedReader getReader(InputStream is, String charSet) {
        if (is == null)
            return null;
        UnicodeReader isr = new UnicodeReader(is, charSet);
        return new BufferedReader(isr);
    }

    /**
     * 获得相对于一个class的所在路径的相对路径的文件资源
     * @param source   class
     * @param fileName 文件相对路径
     * @param charSet  编码
     * @return BufferedReader 如果文件不存在，返回null
     */
    public static BufferedReader getReader(Class<?> source, String fileName, String charSet) {
        InputStream is = source.getResourceAsStream(fileName);
        if (is == null) {
            is = source.getClassLoader().getResourceAsStream(toClassLoaderResourcePath(source, fileName));
        }
        if (is == null)
            return null;
        UnicodeReader isr = new UnicodeReader(is, charSet);
        return new BufferedReader(isr);
    }

    /**
     * ClassLoader resource不用/开头。这里将c转换过去
     * @param fileName
     * @return
     */
    public static String toClassLoaderResourcePath(Class<?> c, String fileName) {
        if (fileName.startsWith("/"))
            return fileName.substring(1);
        String path = c.getPackage().getName().replace('.', '/');
        return path.concat("/").concat(fileName);
    }

    /**
     * 文件过滤器
     * @author jiyi
     */
    public static abstract class FileFilterEx implements FileFilter {
        /**
         * 是否跳出当前文件夹搜索 每次运行完成accept方法后，程序会执行此方法，如果返回true则将停止在此目录中的搜索。
         * @return true中断文件搜索，false继续搜索。
         */
        protected boolean breakFolder(File root) {
            return false;
        }

        ;
    }

    /**
     * 在指定目录下搜索单个文件
     * @param root
     * @param filter 过滤条件
     * @return File 返回文件
     * @Title: findFile
     */
    public static File findFile(File root, FileFilterEx filter) {
        if (root == null || !root.exists())
            return null;
        boolean breakThisFolder = false;
        for (File f : root.listFiles()) {
            if (!breakThisFolder) {
                if (filter.accept(f)) {
                    return f;
                }
                breakThisFolder = filter.breakFolder(root);
            }
            if (f.isDirectory()) {
                File result = findFile(f, filter);
                if (result != null)
                    return result;
            }
        }
        return null;
    }

    /**
     * 在指定目录下搜索文件
     * @param root   要搜索的目录
     * @param filter 文件过滤器
     * @return 搜索到的所有文件
     */
    public static Collection<File> findFiles(File root, FileFilterEx filter) {
        if (root == null || !root.exists())
            return null;
        List<File> result = new ArrayList<File>();
        boolean breakThisFolder = false;
        for (File f : root.listFiles()) {
            if (!breakThisFolder) {
                if (filter.accept(f)) {
                    result.add(f);
                }
                breakThisFolder = filter.breakFolder(root);
            }
            if (f.isDirectory()) {
                result.addAll(findFiles(f, filter));
            }
        }
        return result;
    }

    /**
     * 在指定目录下搜索单个文件
     * @param root         要搜索的目录
     * @param name         搜索的文件名称（完全匹配）
     * @param acceptFolder 是否搜索文件夹
     * @return File 返回文件
     */
    public static File findFile(File root, final String name, final boolean acceptFolder) {
        return findFile(root, new FileFilterEx() {
            public boolean accept(File pathname) {
                if (!acceptFolder && pathname.isDirectory())
                    return false;
                return pathname.getName().equals(name);
            }
        });
    }

    /**
     * 将Reader内容读取到内存中的charArray
     * @param reader 输入
     * @return
     * @throws IOException
     */
    public static char[] asCharArray(Reader reader) throws IOException {
        CharArrayWriter cw = new CharArrayWriter(256);
        char[] buf = new char[1024];
        int n;
        try {
            while ((n = reader.read(buf)) > -1) {
                cw.write(buf, 0, n);
            }
        } finally {
            reader.close();
        }
        return cw.toCharArray();
    }

    /**
     * 将Reader的内容读取为一个字符串
     * @param reader
     * @return
     * @throws IOException
     */
    public static String asString(Reader reader) throws IOException {
        return asString(reader, true);
    }

    /**
     * 将Reader内容读取为字符串
     * @param reader
     * @param close  关闭reader
     * @return
     * @throws IOException
     */
    public static String asString(Reader reader, boolean close) throws IOException {
        if (reader == null)
            return null;
        StringBuilder sb = new StringBuilder(128);
        char[] buf = new char[1024];
        int n;
        try {
            while ((n = reader.read(buf)) > -1) {
                sb.append(buf, 0, n);
            }
        } finally {
            if (close)
                reader.close();
        }
        return sb.toString();
    }

    public static String asString(File pStream, String charset) throws IOException {
        return asString(getReader(pStream, charset));
    }

    /**
     * 将指定位置的数据读出成为文本
     * @param url     资源位置
     * @param charset 字符编码，可以传入null
     * @return 读到的文本
     * @throws IOException IO操作异常
     **/
    public static String asString(URL url, String charset) throws IOException {
        if (url == null)
            return null;
        return asString(url.openStream(), charset, true);
    }

    /**
     * 将输入流转化为String .(使用缺省的字符集)
     * <p>
     * 最简单的获取系统资源转换为String的写法如下： <code>
     * IOUtils.asString(ClassLoader.getSystemResourceAsStream(filename))
     * </code>
     */
    public static String asString(InputStream pStream) throws IOException {
        return asString(pStream, null, true);
    }

    /**
     * 将输入流转化为String
     * @param pStream   The input stream to read.
     * @param pEncoding The character encoding, typically "UTF-8".
     * @param close     close the in stream?
     */
    public static String asString(InputStream pStream, String pEncoding, boolean close) throws IOException {
        if (pStream == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        copy(pStream, baos, close);
        if (pEncoding == null) {
            return baos.toString();
        } else {
            return baos.toString(pEncoding);
        }
    }

    /**
     * 获得配置文件的项目。配置文件用= :等分隔对，语法同properties文件
     * <p>
     * 使用此方法可以代替使用JDK中的{@link java.util.Properties}工具。因为Properties操作中往往有以下不便
     * <ol>
     * <li>在遍历时，由于Properties继承了Map&lt;Object,Object&gt;泛型，不得不编写强制类型转换的代码。</li>
     * <li>Properties继承了Hashtable性能低下，此外如果getProperty(null)还会抛出异常。</li>
     * <li>Properties中的数据是乱序的，无法保持原先在文件中出现的顺序</li>
     * <li>Properties保留了基于InputStream的接口，使用时容易出现编码错误</li>
     * </ol>
     * 因此，建议在加载.properties文件时，不要使用JDK中的{@link java.util.Properties}。
     * <s>彻底淘汰落后的java.util.Properties</s>
     * @param in 要读取的资源
     * @return 文件中的键值对信息。
     */
    public static Map<String, String> loadProperties(URL in) {
        return loadProperties(in, false);
    }

    public static Map<String, String> loadProperties(URL in, Boolean supportSection) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        loadProperties(getReader(in, null), result, supportSection);
        return result;
    }

    /**
     * 获得配置文件的项目。配置文件用= :等分隔对，语法同properties文件
     * <p>
     * 使用此方法可以代替使用JDK中的{@link java.util.Properties}工具。因为Properties操作中往往有以下不便
     * <ol>
     * <li>在遍历时，由于Properties继承了Map&lt;Object,Object&gt;泛型，不得不编写强制类型转换的代码。</li>
     * <li>Properties继承了Hashtable性能低下，此外如果getProperty(null)还会抛出异常。</li>
     * <li>Properties中的数据是乱序的，无法保持原先在文件中出现的顺序</li>
     * <li>Properties保留了基于InputStream的接口，使用时容易出现编码错误</li>
     * </ol>
     * 因此，建议在加载.properties文件时，不要使用JDK中的{@link java.util.Properties}。
     * <s>彻底淘汰落后的java.util.Properties</s>
     * @param in 要读取的数据流。注意读取完成后流会被关闭。
     * @return 文件中的键值对信息。
     */
    public static Map<String, String> loadProperties(Reader in) {
        return loadProperties(in, false);
    }

    /**
     * 获得配置文件的项目。配置文件用= :等分隔对，语法同properties文件
     * <p>
     * 使用此方法可以代替使用JDK中的{@link java.util.Properties}工具。因为Properties操作中往往有以下不便
     * <ol>
     * <li>在遍历时，由于Properties继承了Map&lt;Object,Object&gt;泛型，不得不编写强制类型转换的代码。</li>
     * <li>Properties继承了Hashtable性能低下，此外如果getProperty(null)还会抛出异常。</li>
     * <li>Properties中的数据是乱序的，无法保持原先在文件中出现的顺序</li>
     * <li>Properties保留了基于InputStream的接口，使用时容易出现编码错误</li>
     * </ol>
     * 因此，建议在加载.properties文件时，不要使用JDK中的{@link java.util.Properties}。
     * <s>彻底淘汰落后的java.util.Properties</s>
     * @param in             要读取的数据流。注意读取完成后流会被关闭。
     * @param supportSection 支持分节。window下有一种很类似properties格式的配置文件INI。INI文件中可以使用[section]
     *                       对配置划分小节。开启此开关后，解析时会将当前节名称和配置名拼在一起。 形成 {@code 节名|配置名}的格式。
     * @return 文件中的键值对信息。
     */
    public static Map<String, String> loadProperties(Reader in, Boolean supportSection) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        loadProperties(in, result, supportSection);
        return result;
    }

    /*
     * Read in a "logical line" from an InputStream/Reader, skip all comment and
     * blank lines and filter out those leading whitespace characters ( , and )
     * from the beginning of a "natural line". Method returns the char length of
     * the "logical line" and stores the line in "lineBuf".
     */
    static final class LineReader {
        private char[] inCharBuf;
        private char[] lineBuf = new char[1024];
        private int inLimit = 0;
        private int inOff = 0;
        private Reader reader;

        public LineReader(Reader reader) {
            this.reader = reader;
            inCharBuf = new char[8192];
        }

        int readLine() throws IOException {
            int len = 0;
            char c = 0;

            boolean skipWhiteSpace = true;
            boolean isCommentLine = false;
            boolean isNewLine = true;
            boolean appendedLineBegin = false;
            boolean precedingBackslash = false;
            boolean skipLF = false;

            while (true) {
                if (inOff >= inLimit) {
                    inLimit = reader.read(inCharBuf);
                    inOff = 0;
                    if (inLimit <= 0) {
                        if (len == 0 || isCommentLine) {
                            return -1;
                        }
                        return len;
                    }
                }
                c = inCharBuf[inOff++];

                if (skipLF) {
                    skipLF = false;
                    if (c == '\n') {
                        continue;
                    }
                }
                if (skipWhiteSpace) {
                    if (c == ' ' || c == '\t' || c == '\f') {
                        continue;
                    }
                    if (!appendedLineBegin && (c == '\r' || c == '\n')) {
                        continue;
                    }
                    skipWhiteSpace = false;
                    appendedLineBegin = false;
                }
                if (isNewLine) {
                    isNewLine = false;
                    if (c == '#' || c == '!') {
                        isCommentLine = true;
                        continue;
                    }
                }

                if (c != '\n' && c != '\r') {
                    lineBuf[len++] = c;
                    if (len == lineBuf.length) {
                        int newLength = lineBuf.length * 2;
                        if (newLength < 0) {
                            newLength = Integer.MAX_VALUE;
                        }
                        char[] buf = new char[newLength];
                        System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
                        lineBuf = buf;
                    }
                    // flip the preceding backslash flag
                    if (c == '\\') {
                        precedingBackslash = !precedingBackslash;
                    } else {
                        precedingBackslash = false;
                    }
                } else {
                    // reached EOL
                    if (isCommentLine || len == 0) {
                        isCommentLine = false;
                        isNewLine = true;
                        skipWhiteSpace = true;
                        len = 0;
                        continue;
                    }
                    if (inOff >= inLimit) {
                        inLimit = reader.read(inCharBuf);
                        inOff = 0;
                        if (inLimit <= 0) {
                            return len;
                        }
                    }
                    if (precedingBackslash) {
                        len -= 1;
                        // skip the leading whitespace characters in following
                        // line
                        skipWhiteSpace = true;
                        appendedLineBegin = true;
                        precedingBackslash = false;
                        if (c == '\r') {
                            skipLF = true;
                        }
                    } else {
                        return len;
                    }
                }
            }
        }
    }

    /*
     * Jiyi 2015-9-14日修改。为了兼容windows应用ini的结构（带小节） 故将小节用 | 符号添加在每个key前方
     *
     * @param lr
     *
     * @param map
     *
     * @throws IOException
     */
    private static void load0(LineReader lr, Map<String, String> map, Boolean supportSection) throws IOException {
        char[] convtBuf = new char[1024];
        int limit;
        int keyLen;
        int valueStart;
        char c;
        boolean hasSep;
        boolean precedingBackslash;
        String currentSection = null;

        while ((limit = lr.readLine()) >= 0) {
            c = 0;
            keyLen = 0;
            valueStart = limit;
            hasSep = false;

            precedingBackslash = false;
            while (keyLen < limit) {
                c = lr.lineBuf[keyLen];
                // need check if escaped.
                if ((c == '=' || c == ':') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    hasSep = true;
                    break;
                } else if ((c == ' ' || c == '\t' || c == '\f') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    break;
                }
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash;
                } else {
                    precedingBackslash = false;
                }
                keyLen++;
            }
            while (valueStart < limit) {
                c = lr.lineBuf[valueStart];
                if (c != ' ' && c != '\t' && c != '\f') {
                    if (!hasSep && (c == '=' || c == ':')) {
                        hasSep = true;
                    } else {
                        break;
                    }
                }
                valueStart++;
            }
            String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
            String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
            if (supportSection == null) {
                supportSection = isSection(key);
            }
            if (supportSection && value.length() == 0 && isSection(key)) {
                currentSection = key.length() > 2 ? key.substring(1, key.length() - 1) : null;
            } else {
                if (currentSection == null) {
                    map.put(key, value);
                } else {
                    map.put(currentSection + "|" + key, value);
                }
            }
        }
    }

    private static boolean isSection(String key) {
        if (key == null || key.length() < 2) {
            return false;
        }
        return key.charAt(0) == '[' && key.charAt(key.length() - 1) == ']';
    }

    /*
     * Converts encoded &#92;uxxxx to unicode chars and changes special saved
     * chars to their original forms
     */
    private static String loadConvert(char[] in, int off, int len, char[] convtBuf) {
        if (convtBuf.length < len) {
            int newLen = len * 2;
            if (newLen < 0) {
                newLen = Integer.MAX_VALUE;
            }
            convtBuf = new char[newLen];
        }
        char aChar;
        char[] out = convtBuf;
        int outLen = 0;
        int end = off + len;

        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                aChar = in[off++];
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = in[off++];
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                        }
                    }
                    out[outLen++] = (char) value;
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    out[outLen++] = aChar;
                }
            } else {
                out[outLen++] = (char) aChar;
            }
        }
        return new String(out, 0, outLen);
    }

    private static String saveConvert(String theString, boolean isKey, int option) {
        if (isKey && option < 0) {
            return theString;
        } else if (isKey == false && option < 1) {
            return theString;
        }
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuilder outBuffer = new StringBuilder(bufLen);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch (aChar) {
                case ' ':
                    if (x == 0 || isKey)
                        outBuffer.append('\\');
                    outBuffer.append(' ');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\');
                    outBuffer.append(aChar);
                    break;
                default:
                    outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }

    /*
     * 内部使用,properties文件读取
     */
    static final void loadProperties(Reader in, Map<String, String> map, Boolean supportSecion) {
        if (in == null)
            return;
        try {
            load0(new LineReader(in), map, supportSecion);
        } catch (Exception e1) {
            LogUtil.exception(e1);
        } finally {
            closeQuietly(in);
        }
    }

    /**
     * 得到文件的扩展名（小写如果没有则返回空字符串）。如果传入的文件名包含路径，分析时会考虑最后一个\或/字符后满的部分才作为文件名。
     * @param fileName
     * @return
     */
    public static String getExtName(String fileName) {
        int dashIndex1 = fileName.lastIndexOf('/');
        int dashIndex2 = fileName.lastIndexOf('\\');
        int dash = Math.max(dashIndex1, dashIndex2);// 获得最后一个斜杠的位置

        int pos = fileName.lastIndexOf(".");
        if (pos > -1 && pos > dash) {
            return fileName.substring(pos + 1).toLowerCase();
        } else {
            return "";
        }
    }

    /**
     * 得到文件名除去扩展名的部分。如果传入的文件名包含路径，分析时会考虑最后一个\或/字符后满的部分才作为文件名。 去除扩展名后返回包含路径的部分。
     * @param fileName
     * @return
     */
    public static String removeExt(String fileName) {
        int dashIndex1 = fileName.lastIndexOf('/');
        int dashIndex2 = fileName.lastIndexOf('\\');
        int dash = Math.max(dashIndex1, dashIndex2);// 获得最后一个斜杠的位置
        int pos = fileName.lastIndexOf('.');
        if (pos > -1 && pos > dash) {
            return fileName.substring(0, pos);
        }
        return fileName;
    }

}
