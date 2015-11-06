package com.yidian.push.servlets;

import com.yidian.push.config.Config;
import com.yidian.push.recommend_gen.Generator;
import com.yidian.push.response.SlowResponse;
import com.yidian.push.util.HttpHelper;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by tianyuzhi on 15/11/5.
 */
@Log4j
public class SlowGenerator extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SlowResponse recordResponse = new SlowResponse();
        String sleepTime = req.getParameter("sleep_time");
        int timeInSeconds = Config.getInstance().getRecommendGeneratorConfig().getSleepTimeInSeconds();
        if (StringUtils.isNotEmpty(sleepTime)) {
            try {
                timeInSeconds = Integer.parseInt(sleepTime);
            } catch (Exception e) {
                log.error("invalid sleep_time, which should be a number, just use the default 10s ");
            }
        }


        recordResponse.markSuccess();
        recordResponse.setDescription("got the slow request");

        HttpHelper.setResponseParameters(resp, recordResponse);
        // TODO: generator .SlowALL
        try {
            log.info("got sleep request");
            Generator.sleep(timeInSeconds);
            log.info("done the sleep request");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
