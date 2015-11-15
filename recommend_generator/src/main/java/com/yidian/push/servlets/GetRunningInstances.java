package com.yidian.push.servlets;

import com.yidian.push.config.Config;
import com.yidian.push.recommend_gen.Generator;
import com.yidian.push.recommend_gen.OnlineGenerator;
import com.yidian.push.recommend_gen.RunningInstance;
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
public class GetRunningInstances extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Response recordResponse = new Response();
        int generatorNum = Generator.getRunningInstancesNumber();
        int onlineGeneratorNum = OnlineGenerator.getRunningInstancesNumber();
        int totalRunningNum = RunningInstance.getRunningNumber();

        recordResponse.markSuccess();
        recordResponse.setDescription("totalRunningNum: " + totalRunningNum + "\ngeneratorNum: " + generatorNum + "\n onlineGeneratorNum: " + onlineGeneratorNum);

        HttpHelper.setResponseParameters(resp, recordResponse);
        log.info("done the sleep request");
    }
}
