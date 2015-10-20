package com.yidian.push.recommend_gen;

import com.yidian.push.data.PushType;
import com.yidian.push.utils.GsonFactory;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 15/10/19.
 */
public class GeneratorTest {

    @Test
    public void test(){
        System.out.println(StringUtils.isNotEmpty(null));
    }

    @Test
    public void test2() {
        List<UserPushRecord.DocId_PushType> list = new ArrayList<>();
        list.add(new UserPushRecord.DocId_PushType("1", PushType.RECOMMEND));
        list.add(new UserPushRecord.DocId_PushType("1", PushType.RECOMMEND_3));
        list.add(new UserPushRecord.DocId_PushType("1", PushType.RECOMMEND_1));
        Collections.sort(list, new Comparator<UserPushRecord.DocId_PushType>() {
            @Override
            public int compare(UserPushRecord.DocId_PushType oa, UserPushRecord.DocId_PushType ob) {
                return oa.pushType.getInt() - ob.pushType.getInt();
            }
        });
        System.out.println(GsonFactory.getNonPrettyGson().toJson(list));
        Collections.sort(list, new Comparator<UserPushRecord.DocId_PushType>() {
            @Override
            public int compare(UserPushRecord.DocId_PushType oa, UserPushRecord.DocId_PushType ob) {
                return ob.pushType.getInt() - oa.pushType.getInt();
            }
        });
        System.out.println(GsonFactory.getNonPrettyGson().toJson(list));
    }
}