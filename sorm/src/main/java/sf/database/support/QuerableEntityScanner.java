package sf.database.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.smallframework.spring.path.SpringClassPathScanning;
import org.springframework.asm.ClassReader;
import org.springframework.core.io.Resource;
import sf.common.log.LogUtil;
import sf.database.DBObject;
import sf.database.dao.DBClient;
import sf.database.meta.ColumnMapping;
import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;
import sf.tools.ArrayUtils;
import sf.tools.IOUtils;
import sf.tools.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

/**
 * 自动扫描工具，在构造时可以根据构造方法，自动的将继承DataObject的类检查出来，并载入
 * @author Administrator
 */
public class QuerableEntityScanner {

    public static final Set<String> dynamicEnhanced = new HashSet<String>();

    // implClasses
    private String[] implClasses = new String[]{DBObject.class.getName()};
    /**
     * 是否扫描子包
     */
    private boolean scanSubPackage = true;

    /**
     * 是否创建不存在的表
     */
    private boolean createTable = true;

    /**
     * 是否修改存在的表
     */
    private boolean alterTable = true;

    /**
     * 当alterTable=true时，如果修改表时需要删除列，是否允许删除列
     */
    private boolean allowDropColumn;

    /**
     * 是否检查序列
     */
    private boolean checkSequence = true;

    /**
     * 是否检查索引
     */
    private boolean checkIndex = true;

    /**
     * 当创建新表后，是否同时初始化表中的数据
     */
    private boolean initDataAfterCreate = true;
    /**
     * 当扫描到已经存在的表后，是否检查初始化数据。 一般在开发阶段开启
     */
    private boolean initDataIfTableExists = false;

    /**
     * 连接
     */
    private DBClient dbClient;
    /**
     * 扫描包
     */
    private String[] packageNames = {"sf"};

    public String[] getPackageNames() {
        return packageNames;
    }

    public void setPackageNames(String packageNames) {
        this.packageNames = packageNames.split(",");
    }

    public boolean isScanSubPackage() {
        return scanSubPackage;
    }

    public String[] getImplClasses() {
        return implClasses;
    }

    /**
     * 设置多个DataObject类
     * @param implClasses
     */
    public void setImplClasses(String[] implClasses) {
        this.implClasses = implClasses;
    }

    @SuppressWarnings("rawtypes")
    public void setImplClasses(Class... implClasses) {
        String[] result = new String[implClasses.length];
        for (int i = 0; i < implClasses.length; i++) {
            result[i] = implClasses[i].getName();
        }
        this.implClasses = result;
    }

    public void setScanSubPackage(boolean scanSubPackage) {
        this.scanSubPackage = scanSubPackage;
    }

    public void doScan() {
        String[] parents = getClassNames();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null)
            cl = QuerableEntityScanner.class.getClassLoader();

        // 开始
        Resource[] classes = SpringClassPathScanning.getClassResources(packageNames);

        // 循环所有扫描到的类
        Map<TableMapping, Boolean> tasks = new HashMap<TableMapping, Boolean>();
        for (Resource s : classes) {
            try {
                ClassReader cr = getClassInfo(cl, s);
                if (cr == null)//NOT found class
                    continue;
                // 根据父类判断
                if (isEntiyClz(cl, parents, cr.getSuperName())) {
                    Class<?> clz = loadClass(cl, cr.getClassName().replace('/', '.'));
                    if (clz != null && DBObject.class.isAssignableFrom(clz) && clz.getAnnotation(Table.class) != null && clz.getAnnotation(Entity.class) != null) {
                        registeEntity(clz, tasks);
                    }
                }
                ;
            } catch (IOException e) {
                LogUtil.exception(e);
            }
        }
        processInit(tasks);
    }

    private boolean isEntiyClz(ClassLoader cl, String[] parents, String superName) throws IOException {
        if ("java/lang/Object".equals(superName)) {
            return false;
        }
        if (ArrayUtils.contains(parents, superName)) {// 是实体
            return true;
        }
        // 读取类
        ClassReader cr = getClassInfo(cl, superName);
        if (cr == null) {
            return false;
        }
        return isEntiyClz(cl, parents, cr.getSuperName());
    }

    private ClassReader getClassInfo(ClassLoader cl, String s) throws IOException {
        URL url = cl.getResource(s.replace('.', '/') + ".class");
        if (url == null)
            return null;
        InputStream stream = url.openStream();
        if (stream == null) {
            LogUtil.error("The class content [" + s + "] not found!");
            return null;
        }
        return new ClassReader(stream);
    }

    private ClassReader getClassInfo(ClassLoader cl, Resource s) throws IOException {
        InputStream stream = s.getInputStream();
        if (stream == null) {
            LogUtil.error("The class content [" + s + "] not found!");
            return null;
        }
        return new ClassReader(stream);
    }


    private Class<?> loadClass(ClassLoader cl, String s) {
        try {
            Class<?> c = cl.loadClass(s);
            return c;
        } catch (ClassNotFoundException e) {
            LogUtil.error("Class not found:" + e.getMessage());
            return null;
        }
    }

    public boolean registeEntity(String... names) {
        if (names == null) return true;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = this.getClass().getClassLoader();
        }

        Map<TableMapping, Boolean> tasks = new HashMap<TableMapping, Boolean>();
        for (String name : names) {
            if (StringUtils.isEmpty(name)) {
                continue;
            }
            Class<?> c;
            try {
                c = cl.loadClass(name);
                registeEntity(c, tasks);
            } catch (ClassNotFoundException e) {
                LogUtil.error("Class not found:" + e.getMessage());
            }
        }
        try {
            processInit(tasks);
            return true;
        } catch (Exception e) {
            LogUtil.exception(e);
            return false;
        }
    }

    private void processInit(Map<TableMapping, Boolean> tasks) {
        try {
            for (Entry<TableMapping, Boolean> entry : tasks.entrySet()) {
                boolean isNew = entry.getValue();
                TableMapping meta = entry.getKey();
                // 初始化表中的数据
                if (isNew && initDataAfterCreate) {
                    URL url = meta.getThisType().getResource(meta.getThisType().getSimpleName() + ".init.json");
                    if (url != null) {
                        try {
                            initData(url, meta, false);
                        } catch (IOException e1) {
                            LogUtil.exception(e1);
                        }
                    }
                } else if (!isNew && initDataIfTableExists) {
                    URL url = meta.getThisType().getResource(meta.getThisType().getSimpleName() + ".init.json");
                    if (url != null) {
                        try {
                            initData(url, meta, true);
                        } catch (IOException e1) {
                            LogUtil.exception(e1);
                        }
                    }
                }
            }
        } finally {

        }
    }

    private void registeEntity(Class<?> c, Map<TableMapping, Boolean> tasks) {
        try {
            TableMapping meta = MetaHolder.getMeta(c);// 用initMeta变为强制初始化。getMeta更优雅一点
            if (meta != null) {
                LogUtil.info("Table [" + meta.getTableName() + "] <--> [" + c.getName() + "]");
            } else {
                LogUtil.error("Entity [" + c.getName() + "] was not mapping to any table.");
            }
            final boolean create = createTable;
            final boolean refresh = alterTable;
            if (create || refresh) {
                doTableDDL(meta, create, refresh, tasks);
            }
        } catch (Throwable e) {
            LogUtil.error("EntityScanner:[Failure]", e);
        }
    }

    private void doTableDDL(TableMapping meta, final boolean create, final boolean refresh, Map<TableMapping, Boolean> tasks) throws SQLException {
        // 不管是否存在，总之先创建一次
        boolean exist = true;
        boolean isNew = false;

        if (create) {
            dbClient.createTable(meta.getThisType());
        } else {

        }

        if (exist) {
            dbClient.refreshTable(meta.getThisType());
        }

        if (!exist) {
            return;
        }
        // 检查Sequence
        if (checkSequence) {
            for (ColumnMapping f : meta.getMetaFields()) {

            }
        }
        tasks.put(meta, isNew);

    }

    public static class InitDataModel {
        public boolean cascade;
        public boolean merge;
        private List<?> data;

        public List<?> getData() {
            return data;
        }

        public void setData(List<?> data) {
            this.data = data;
        }

        public void set(String key, String value) {
            if ("cascade".equals(key)) {
                this.cascade = Boolean.valueOf(value);
            } else if ("merge".equals(key)) {
                this.merge = Boolean.valueOf(value);
            } else {
                LogUtil.warn("Unknown key in file init.json: {}", key);
            }
        }
    }

    /*
     * 数据初始化
     *
     * @param url
     *
     * @param meta
     */
    private void initData(URL url, TableMapping meta, boolean merge) throws IOException {
        LogUtil.info("init data for table {}", meta.getTableName());
        InitDataModel model = parseData(url, meta);
        if (merge || model.merge) {
            if (model.cascade) {

            } else {

            }
            ;
        } else {
            try {
                if (model.cascade) {
                    for (Object o : model.getData()) {

                    }
                } else {

                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private InitDataModel parseData(URL url, TableMapping meta) throws IOException {
        BufferedReader reader = IOUtils.getReader(url, "UTf-8");
        StringWriter sw = new StringWriter(1024);
        InitDataModel result = new InitDataModel();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                int n = line.indexOf(':');
                if (n > 0) {
                    String key = line.substring(1, n).trim().toLowerCase();
                    String value = line.substring(n + 1).trim();
                    result.set(key, value);
                }
            } else {
                sw.write(line);
                sw.write("\n");
                break;
            }
        }
        IOUtils.copy(reader, sw, true);
        String data = sw.toString();
        try {
            List<?> results = JSON.parseArray(data, meta.getThisType());
            result.setData(results);
        } catch (JSONException e) {
            throw new IllegalArgumentException(url.toString() + " is a invalid json file", e);
        }
        return result;
    }

    private String[] getClassNames() {
        List<String> clzs = new ArrayList<String>();
        for (int i = 0; i < implClasses.length; i++) {
            String s = implClasses[i];
            s = StringUtils.trimToNull(s);
            if (s == null)
                continue;
            clzs.add(s.replace('.', '/'));
        }
        return clzs.toArray(new String[clzs.size()]);
    }

    public boolean isAllowDropColumn() {
        return allowDropColumn;
    }

    public void setAllowDropColumn(boolean allowDropColumn) {
        this.allowDropColumn = allowDropColumn;
    }

    public boolean isCreateTable() {
        return createTable;
    }

    public void setCreateTable(boolean createTable) {
        this.createTable = createTable;
    }

    public boolean isAlterTable() {
        return alterTable;
    }

    public void setAlterTable(boolean alterTable) {
        this.alterTable = alterTable;
    }

    public boolean isCheckSequence() {
        return checkSequence;
    }

    public void setCheckSequence(boolean checkSequence) {
        this.checkSequence = checkSequence;
    }

    public void setInitDataAfterCreate(boolean initDataAfterCreate) {
        this.initDataAfterCreate = initDataAfterCreate;
    }

    public boolean isInitDataAfterCreate() {
        return initDataAfterCreate;
    }

    public boolean isCheckIndex() {
        return checkIndex;
    }

    public boolean isInitDataIfTableExists() {
        return initDataIfTableExists;
    }

    public void setInitDataIfTableExists(boolean initDataIfTableExists) {
        this.initDataIfTableExists = initDataIfTableExists;
    }

    public void setCheckIndex(boolean checkIndex) {
        this.checkIndex = checkIndex;
    }

    public DBClient getDbClient() {
        return dbClient;
    }

    public void setDbClient(DBClient dbClient) {
        this.dbClient = dbClient;
    }
}
