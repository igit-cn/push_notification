package com.yidian.push.weather.servlets;

import com.yidian.push.weather.processor.SmartWeather;
import com.yidian.push.weather.response.HistoryResponse;
import com.yidian.push.weather.util.HttpHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by tianyuzhi on 16/1/25.
 */
public class GetHistoryServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HistoryResponse response = new HistoryResponse();
        response.setResult(SmartWeather.getInstance().getCachedAlarmIdDocMapping());
        HttpHelper.setResponseParameters(resp, response);
    }
}
