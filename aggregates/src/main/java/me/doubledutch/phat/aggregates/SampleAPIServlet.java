package me.doubledutch.phat.aggregates;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.*;

public class SampleAPIServlet extends HttpServlet{
	private final Logger log = LogManager.getLogger("SampleAPI");

	private static SampleRunner sampleRunner;

	public static void setSampleRunner(SampleRunner runner){
		sampleRunner=runner;
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1).trim();
			System.out.println("Looking for sample '"+uriPath+"'");
			Writer out=response.getWriter();
			response.setContentType("application/json");
			out.append(sampleRunner.getSample(uriPath).toJSON().toString());
		}catch(Exception e){
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR ,"Internal server error");
		}
		return;
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			JSONObject obj=new JSONObject(readPostBody(request));
			System.out.println("Creating sample task for "+obj.toString());

			Sample sample=sampleRunner.startSample(obj);

			
			
			response.setContentType("application/json");
			response.getWriter().append("{\"result\":\"ok\",\"sample\":"+sample.toJSON().toString()+"}");
		}catch(Exception e){
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR ,"Internal server error");
		}
		return;
	}

	private String readPostBody(final HttpServletRequest request) throws IOException{
		BufferedReader reader = request.getReader();
		StringBuilder buf=new StringBuilder();
		char[] data=new char[32768];
		int num=reader.read(data);
		while(num>-1){
			buf.append(data,0,num);
			num=reader.read(data);
		}
		return buf.toString();
	}
}