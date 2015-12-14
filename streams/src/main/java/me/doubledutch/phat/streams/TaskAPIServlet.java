package me.doubledutch.phat.streams;

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

public class TaskAPIServlet extends HttpServlet{
	private final Logger log = LogManager.getLogger("TaskAPI");

	private static TaskHandler taskHandler;
	private static StreamHandler streamHandler;
	private static String importPath;
	private static String exportPath;

	public static void setTaskHandler(TaskHandler taskHandlerArg){
		taskHandler=taskHandlerArg;
	}

	public static void setStreamHandler(StreamHandler streamHandlerArg){
		streamHandler=streamHandlerArg;
	}

	public static void setPaths(String importPathArg,String exportPathArg){
		importPath=importPathArg;
		exportPath=exportPathArg;
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			Writer out=response.getWriter();
			response.setContentType("application/json");
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1).trim();
			String[] splitPath=uriPath.split("/");
			if(uriPath.length()==0){
				JSONObject result=new JSONObject();
				// List tasks
				JSONArray tasks=new JSONArray();
				for(Task task:taskHandler.getTasks()){
					tasks.put(task.toJSON());
				}
				result.put("tasks",tasks);
				JSONArray files=new JSONArray();
				File ftest=new File(importPath);
				if(ftest.exists()){
					for(String file:ftest.list()){
						JSONObject obj=new JSONObject();
						obj.put("filename",file);
						ftest=new File(importPath+file);
						obj.put("size",ftest.length());
						files.put(obj);
					}
				}
				result.put("files",files);
				out.write(result.toString());
				return;
			}else if(splitPath.length==1){
				// Get one task
				String id=splitPath[0];
				// TODO: implement this
			}else{
				response.sendError(HttpServletResponse.SC_NOT_FOUND,"You must specify both topic and location.");
			}
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
			System.out.println("Creating task for "+obj.toString());
			obj.put("state",0);
			obj.put("id",UUID.randomUUID().toString());
			Stream stream=streamHandler.getOrCreateStream(obj.getString("topic"));
			obj.put("checkpoint",stream.getCount());
			obj.put("started",System.currentTimeMillis());
			obj.put("finished",-1);

			String id=taskHandler.createTask(obj);
			response.setContentType("application/json");
			response.getWriter().append("{\"result\":\"ok\",\"id\":\""+id+"\"}");
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