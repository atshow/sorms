package sf.ext.gen;


import sf.database.dbinfo.TableInfo;

public interface CodeGen {
    public void genCode(String entityPkg, String entityClass, TableInfo tableDesc, GenConfig config, boolean isDisplay);
}
