package com.yidian.push.weather.servlets;

import com.yidian.push.config.Config;
import com.yidian.push.weather.data.Alarm;
import com.yidian.push.weather.exception.UrlGenerationException;
import com.yidian.push.weather.processor.SmartWeather;
import com.yidian.push.weather.response.AlarmResponse;
import com.yidian.push.weather.util.HttpHelper;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by tianyuzhi on 16/1/25.
 */
@Log4j
public class GetAlarmServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String city = req.getParameter("city");
        String areaId = req.getParameter("areaId");
        AlarmResponse response = new AlarmResponse();

        if (StringUtils.isEmpty(city) && StringUtils.isEmpty(areaId)) {
            response.setDescription("city or areaId not set");
            HttpHelper.setResponseParameters(resp, response);
        }
        if (StringUtils.isNotEmpty(areaId)) {
            try {
                List<Alarm> alarmList = SmartWeather.getInstance().getWeather().getAreaIdAlarms(areaId);
                response.setResult(alarmList);
                response.markSuccess();
            } catch (UrlGenerationException e) {
                log.error("get alarm for city[" + city + "] with error " + ExceptionUtils.getFullStackTrace(e));
                response.setDescription(e.getMessage());
            }
        }
        else {
            try {
                List<Alarm> alarmList = SmartWeather.getInstance().getWeather().getAreaAlarms(city);
                response.setResult(alarmList);
                response.markSuccess();
            } catch (UrlGenerationException e) {
                log.error("get alarm for city[" + city + "] with error " + ExceptionUtils.getFullStackTrace(e));
                response.setDescription(e.getMessage());
            }
        }
        HttpHelper.setResponseParameters(resp, response);
    }
}
