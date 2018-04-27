/*
 * JEF - Copyright 2009-2010 Jiyi (mr.jiyi@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sf.database.jdbc.result;

import sf.database.util.DBUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultSets {
    private ResultSets() {
    }

    /**
     * 显示sql的resultset
     * @param rs    结果集
     * @param limit 限制
     */
    public static void showResult(ResultSet rs, int limit) {
        if (rs == null) {
            return;
        }
        showResult(rs, limit, true);
    }

    /**
     * 以文本显示SQL结果
     * @param rs      结果集
     * @param limit   　限制
     * @param closeIt 是否关闭
     */
    public static void showResult(ResultSet rs, int limit, boolean closeIt) {

    }

    public static List<Map<String, Object>> toMap(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        while (rs.next()) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 1; i <= cols; i++) {
                map.put(meta.getColumnName(i), rs.getObject(i));
            }
            result.add(map);
        }
        DBUtils.closeQuietly(rs);
        return result;
    }

    public static List<Object> toObject(ResultSet rs, int column) throws SQLException {
        List<Object> result = new ArrayList<Object>();
        ResultSetMetaData meta = rs.getMetaData();
        result.add("(" + meta.getColumnName(column) + ")");
        while (rs.next()) {
            result.add(rs.getObject(column));
        }
        DBUtils.closeQuietly(rs);
        return result;
    }

    public static List<Object> toObjectList(ResultSet wrapper, int column, int maxReturn) throws SQLException {
        if (maxReturn == 0) maxReturn = Integer.MAX_VALUE;
        int count = 0;
        List<Object> data = new ArrayList<Object>();
        while (wrapper.next() && count < maxReturn) {
            data.add(wrapper.getObject(column));
            count++;
        }
        return data;
    }

    /**
     * 获取指定列的String数据，整体返回一个List<String>
     * @param rs
     * @param column
     * @param maxReturn
     * @return List<String> 返回类型
     * @throws SQLException
     */
    public static List<String> toStringList(ResultSet rs, String column, int maxReturn) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        String c = null;
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            String c2 = rsmd.getColumnName(i);
            if (c2.equalsIgnoreCase(column)) {
                c = c2;
                break;
            }
        }
        if (c == null) {
            throw new SQLException("The column does not exist in the resultset: " + column);
        }
        column = c;
        int count = 0;
        List<String> data = new ArrayList<String>();
        while (rs.next() && count <= maxReturn) {
            data.add(rs.getString(column));
            count++;
        }
        return data;
    }
}
