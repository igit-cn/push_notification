package com.yidian.push.generator.gen.config;

import com.yidian.push.data.HostPortDB;
import com.yidian.push.generator.Table;
import com.yidian.push.generator.Task;
import com.yidian.push.generator.gen.SqlUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * Created by tianyuzhi on 15/6/18.
 */
@Getter
@Setter
public class PushAllConfig {
    String table;
    Task task;
    HostPortDB hostPortDB;
    Set<Integer> bucketIds = null;
    Range userRange = null;
    int batchSize=10000;
    int todayFirstUserId = 0;

    public PushAllConfig() {}

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
        if (null != userRange) {
            priority.append(" and userid > ").append(userRange.getStart()).append(" userid <= ").append(userRange.getEnd());
        }
        if (todayFirstUserId > 0) {
            priority.append(" and userid < ").append(todayFirstUserId);
        }
        if (Table.isIPhone(table)) {
            //select userid, token, push_level, appid, enable, time_zone, version from %s where (enable = 1 or enable & %d) %s order by userid desc, version desc

            sql.append("select userid, token, push_level, appid," +
                    " enable, time_zone, version from PUSH where (enable = 1 or enable & ")
                    .append(task.getPushType().getInt()).append(")").append(priority.toString())
                    .append(" order by userid desc, version desc");
        }
        else {
            sql.append("select userid, token, push_level, appid," +
                    " enable, time_zone from PUSH_FOR_ANDROID where (enable = 1 or enable & ")
                    .append(task.getPushType().getInt()).append(")").append(priority.toString())
                    .append(" order by userid desc, version desc");
        }
        return sql.toString();
    }
}
