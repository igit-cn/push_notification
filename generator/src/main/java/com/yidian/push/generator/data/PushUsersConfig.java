package com.yidian.push.generator.data;

import com.yidian.push.data.HostPortDB;
import com.yidian.push.data.Platform;
import com.yidian.push.generator.util.SqlUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by tianyuzhi on 15/6/18.
 */
@Getter
@Setter
public class PushUsersConfig {
    Task task;
    HostPortDB hostPortDB;
    transient List<Long> users = null;
    Set<Integer> bucketIds = null;
    int batchSize = 10000;
    long todayFirstUserId = 0;
    String file;

    public PushUsersConfig() {
    }

    public String genSql() {
        StringBuilder priority = new StringBuilder();
        StringBuilder sql = new StringBuilder();
        List<String> appIdInclude = task.getAppIdInclude();
        List<String> appIdExclude = task.getAppIdExclude();
        if (null != appIdInclude && appIdInclude.size() > 0) {
            priority.append(" and appid in ").append(SqlUtil.genQuotedStringList(appIdInclude));
        }
        else if (null != appIdExclude && appIdExclude.size() > 0) {
            priority.append(" and appid not in ").append(SqlUtil.genQuotedStringList(appIdExclude));
        }

        if (null != users && users.size() > 0) {
            priority.append(" and userid in ").append(SqlUtil.genQuotedLongList(users));
        }

//        if (todayFirstUserId > 0) {
//            priority.append(" and userid < ").append(todayFirstUserId);
//        }
        String table = task.getTable();
        if (Platform.isIPhone(table)) {
            //select userid, token, push_level, appid, enable, time_zone, version from %s where (enable = 1 or enable & %d) %s order by userid desc, version desc

            sql.append("select userid, token, push_level, appid," +
                    " enable, time_zone, version from " + table + " where (enable = 1 or enable & ")
                    .append(task.getPushType().getInt()).append(")").append(priority.toString())
                    .append(" order by userid desc, version desc");
        }
        else {
            sql.append("select userid, token, push_level, appid," +
                    " enable, time_zone, version from " + table + " where (enable = 1 or enable & ")
                    .append(task.getPushType().getInt()).append(")").append(priority.toString())
                    .append(" order by userid desc, version desc");
        }
        return sql.toString();
    }

}
