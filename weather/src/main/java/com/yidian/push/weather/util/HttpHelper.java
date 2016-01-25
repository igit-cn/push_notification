package com.yidian.push.weather.util;

import com.yidian.push.utils.GsonFactory;

import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class HttpHelper {
	
	public static void setResponseParameters(ServletResponse response, Object obj) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println(GsonFactory.getNonPrettyGson().toJson(obj));
		out.close();
	}
}
