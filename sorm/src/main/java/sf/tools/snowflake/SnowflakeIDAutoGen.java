package sf.tools.snowflake;

import java.util.Map;


public class SnowflakeIDAutoGen {

    SnowflakeIDWorker defaultWork = new SnowflakeIDWorker(0, 0);
    Map<String, SnowflakeIDWorker> map = null;

    public SnowflakeIDAutoGen() {

    }

    public SnowflakeIDAutoGen(Map<String, SnowflakeIDWorker> map) {
        this.map = map;

    }

    /**
     * 生成id
     * @param params
     * @return
     */
    public Long nextID(String params) {
        if (params == null || params.length() == 0) {
            return defaultWork.nextId();
        } else {
            SnowflakeIDWorker worker = map.get(params);
            if (worker == null) {
                throw new NullPointerException("params " + params + " can not found id worker");
            }
            return worker.nextId();
        }
    }

}
