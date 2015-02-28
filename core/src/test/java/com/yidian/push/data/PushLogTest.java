package com.yidian.push.data;

import com.yidian.push.utils.DateUtil;
import org.testng.annotations.Test;

public class PushLogTest {

    @Test
    public void testGetUidSegmentSize() throws Exception {
        assert PushLog.UID_SEGMENT == PushLog.getUidSegmentSize("2014-09-04");

    }

    @Test
    public void testGetPushLogSegmentSize() throws Exception {
        assert PushLog.LOG_SEGMENT_OLD == PushLog.getPushLogSegmentSize(PushLog.LOG_SEGMENT_OLD_DATE);
        assert PushLog.LOG_SEGMENT == PushLog.getPushLogSegmentSize(
                DateUtil.dateToYYYY_MM_DD(
                        DateUtil.incrDate(
                                DateUtil.YYYY_MM_DDToDate(PushLog.LOG_SEGMENT_OLD_DATE)
                        )
                )
        );

    }

    @Test
    public void testEncodeDocId() throws Exception {
        System.out.println(new String (PushLog.encodeDocId("news_506ee45d3c311907cb7b8edf089f0f28")));
        System.out.println(new String (PushLog.encodeDocId("08Af0D28")));
        assert "#D######08Af0D28".equals(new String (PushLog.encodeDocId("08Af0D28")));
        System.out.println(new String (PushLog.encodeDocId("l_0007000100020003000400050006")));

    }

    @Test
    public void testDecodeDocId() throws Exception {
        String[] docids = {"news_506ee45d3c311907cb7b8edf089f0f28", "08Af0D28",
                "l_0007", "l_00070001", "l_000700010002", "l_0007000100020003",
                "l_00070001000200030004", "l_000700010002000300040005",  "l_0007000100020003000400050006"};
        //String[] docids = {"list_506ee45d3c311907cb7b8edf"};

        for(String docid : docids) {
            byte[] bytes = PushLog.encodeDocId(docid);
            String tmpDocId =  PushLog.decodeDocId(bytes);
            System.out.printf("org:[%s], new:[%s]\n", docid, tmpDocId);
            assert docid.equals(tmpDocId);
        }

    }

    @Test
    public void testDecodeDocId2() throws Exception {
        String[] docids = {"news_506ee45d3c311907cb7b8edf089f0f28", "08Af0D28",
                "l_0007", "l_00070001", "l_000700010002", "l_0007000100020003",
                "l_00070001000200030004", "l_000700010002000300040005",  "l_0007000100020003000400050006"};
        //String[] docids = {"list_506ee45d3c311907cb7b8edf"};

        for(String docid : docids) {
            byte[] bytes = PushLog.encodeDocId(docid);
            byte[] newBytes = new byte[bytes.length + 2];
            for (int i = 0; i < bytes.length;i++) {
                newBytes[i+1] = bytes[i];
            }
            int start = 1;
            int end = start + bytes.length;
            System.out.println(docid + ","+start + ","+end);

            String tmpDocId =  PushLog.decodeDocId(newBytes, start, end);
            System.out.printf("org:[%s], new:[%s]\n", docid, tmpDocId);
            assert docid.equals(tmpDocId);
        }

    }

    @Test
    public void testGenAppend() throws Exception {

    }


}