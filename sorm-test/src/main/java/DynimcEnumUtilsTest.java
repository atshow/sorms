/**
 * @Project fpd
 * @Title EnumUtils.java
 * @Package com.wanfin.fpd.common.config
 * @Description [[_xxx_]]文件
 * @author Chenh
 * @date 2016年5月30日 上午11:02:33
 * @version V1.0
 */

import sf.tools.enums.DynimcEnumUtils;

import java.util.Arrays;

/** https://bbs.csdn.net/topics/391037532
 * @author Chenh
 * @Description [[_xxx_]] EnumUtils类
 * @date 2016年5月30日 上午11:02:33
 */
public class DynimcEnumUtilsTest {

    private static enum FM {
        a(), b(), c();
    }

    /**
     * 自定义表单模块类型
     */
    public static enum FXModel {
        M_TPL("tpl"),
        M_PRODUCT("product");
        private String key;//类别

        /**
         * @Description [[_xxx_]]构造器
         * @author Chenh
         * @date 2015-1-15 下午3:39:14
         */
        private FXModel(String key) {
            this.key = key;
        }

        public static FXModel getFModelByKey(String key) {
            FXModel[] fmodels = FXModel.values();
            for (FXModel fModel : fmodels) {
                if ((fModel.getKey()).equals(key)) {
                    return fModel;
                }
            }
            return null;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    /**
     * 自定义表单模块类型
     */
    public static enum FXXModel {
        M_TPL("tpl", new FXXModel[]{}),
        M_PRODUCT("product", new FXXModel[]{M_TPL});

        private String key;//类别
        private FXXModel[] modsub;//子模块

        private FXXModel(String key, FXXModel[] modsub) {
            this.key = key;
            this.modsub = modsub;
        }

        public static FXXModel getFXXModelByKey(String key) {
            FXXModel[] fmodels = FXXModel.values();
            for (FXXModel fModel : fmodels) {
                if ((fModel.getKey()).equals(key)) {
                    return fModel;
                }
            }
            return null;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setModsub(FXXModel[] modsub) {
            this.modsub = modsub;
        }

        public FXXModel[] getModsub() {
            return modsub;
        }
    }

    public static void main(String[] args) {
        // Dynamically add 3 new enum instances d, e, f to TestEnum
        DynimcEnumUtils.addEnum(FM.class, "d");
        DynimcEnumUtils.addEnum(FM.class, "e");
        DynimcEnumUtils.addEnum(FM.class, "f");
        System.out.println(Arrays.deepToString(FM.values()));

        DynimcEnumUtils.addEnum(FXModel.class, "M_XX", new Class[]{String.class}, new Object[]{"key"});
        DynimcEnumUtils. addEnum(FXModel.class, "M_YY", new Class[]{String.class}, new Object[]{"yy"});
        System.out.println(Arrays.deepToString(FXModel.values()));

        DynimcEnumUtils.addEnum(FXXModel.class, "M_QQ", new Class[]{String.class, FXXModel[].class}, new Object[]{"yy", new FXXModel[]{FXXModel.M_TPL}});
        System.out.println(Arrays.deepToString(FXXModel.values()));
    }
}