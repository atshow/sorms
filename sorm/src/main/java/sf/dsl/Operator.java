package sf.dsl;

/**
 * 条件运算符 <li>=等于</li> <li>>大于</li> <li>< 小于</li> <li>>=大于等于</li> <li><= 小于等于
 * </li> <li>!= 不等于</li> <li>^= 匹配字符串头</li> <li>$= 匹配字符串尾</li> <li>*=
 * 匹配字符串任意位置</li> <li>in in</li> <li>[] BETWEEN</li>
 */
public enum Operator {
    EQUALS("="), GREAT(">"), LESS("<"), GREAT_EQUALS(">="), LESS_EQUALS("<="), MATCH_ANY("*=", " like "), MATCH_START("^=", " like "), MATCH_END("$=", " like "), IN("in", " in "), NOT_IN("not in"), NOT_EQUALS("!="), BETWEEN_L_L("[]"), IS_NULL("=NULL"), IS_NOT_NULL("!=NULL");

    Operator(String key, String oper) {
        this.key = key;
        this.oper = oper;
    }

    Operator(String key) {
        this.key = key;
        this.oper = key;
    }

    private String key;
    private String oper;

    public String getKey() {
        return key;
    }

    //
    public static Operator valueOfKey(String key) {
        for (Operator o : Operator.values()) {
            if (o.key.equals(key)) {
                return o;
            }
        }
        if ("!=".equals(key)) {
            return Operator.NOT_EQUALS;
        }
        return null;
    }

    public String getOper() {
        return oper;
    }
}