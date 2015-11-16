package com.yidian.push.servlets;

import com.yidian.push.config.Config;
import com.yidian.push.recommend_gen.Generator;
import com.yidian.push.recommend_gen.OnlineGenerator;
import com.yidian.push.response.Response;
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
        Response recordResponse = new Response();
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
        log.info("got sleep request");
        final int finalTimeInSeconds = timeInSeconds;
        long start = System.currentTimeMillis();

        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Generator.sleep(finalTimeInSeconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OnlineGenerator.sleep(finalTimeInSeconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        threadA.start();
        threadB.start();
        try {
            threadA.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            threadB.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("done the sleep request");
        long end = System.currentTimeMillis();
        log.info("ROUND_TIME: slowGenerator elapsed time：" + (end - start) / (1000.0 * 60) + " minute");
    }
}
