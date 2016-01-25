package com.yidian.push.weather.servlets;

import com.yidian.push.weather.processor.SmartWeather;
import com.yidian.push.weather.response.AlarmResponse;
import com.yidian.push.weather.response.SupportedAreaResponse;
import com.yidian.push.weather.util.HttpHelper;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by tianyuzhi on 16/1/25.
 */
public class GetSupportedAreasServlet extends HttpServlet {

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SupportedAreaResponse response = new SupportedAreaResponse();
        response.setResult(SmartWeather.getInstance().getWeather().getIdToAreaMapping());
        response.markSuccess();
        HttpHelper.setResponseParameters(resp, response);
    }
}
