package com.yidian.push.servlets;

import com.yidian.push.response.AddRecordResponse;
import com.yidian.push.utils.HttpHelper;
import com.yidian.push.utils.ZionPoolUtil;
import lombok.extern.log4j.Log4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Log4j
public class AddHistoryServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] pushRecordArray = req.getParameterValues("record");
        AddRecordResponse recordResponse = new AddRecordResponse();
        if (null == pushRecordArray || pushRecordArray.length == 0) {
            recordResponse.markFailure();
            recordResponse.setDescription("no record to add.");
        }
        else {
            recordResponse.markSuccess();
            recordResponse.setDescription("got number of records : " + pushRecordArray.length);
        }
        HttpHelper.setResponseParameters(resp, recordResponse);

        try {
            if (null != pushRecordArray) {
                ZionPoolUtil.addPushHistoryRecords(Arrays.asList(pushRecordArray));
            }
        } catch (Exception e) {
            log.error("write log failed. ");
        }
    }
}

