package me.doubledutch.phat.streams;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.*;
import org.json.*;
import java.util.*;

import me.doubledutch.phat.servlet.*;

public class StreamServer implements Runnable{
	private final Logger log = LogManager.getLogger("StreamServer");

	private Server server;
	private StreamHandler streamHandler;
	private TaskHandler taskHandler;
	
	// Configuration 
	private String bind="*";
	private int port=13400;
	private String location="./data/";
	private String importLocation="./import/";
	private String exportLocation="./export/";
	private int batch=32;
	private int lag=25;

	public StreamServer(String filename){
		try{
			log.info("Starting");
			// Load configuration
			if(filename!=null){
				setConfiguration(new JSONObject(readFully(filename)));
			}else{
				setConfiguration(new JSONObject());
			}
			// Start the service
			streamHandler=new StreamHandler(location,batch,lag);
			StreamAPIServlet.setStreamHandler(streamHandler);
			
			TaskHandler.setPaths(importLocation,exportLocation);
			Task.setPaths(importLocation,exportLocation);
			taskHandler=new TaskHandler(streamHandler);

			TaskAPIServlet.setTaskHandler(taskHandler);
			TaskAPIServlet.setStreamHandler(streamHandler);
			TaskAPIServlet.setPaths(importLocation,exportLocation);
			startServlets();
			// Setup shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread(this));
			log.info("Ready!");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * This method is added as a shutdown hook to the runtime.
	 */
	public void run(){
		// Stop servlets
		try{
			server.stop();
		}catch(Exception e){}
		// Stop streamHandler
		try{
			streamHandler.stop();
		}catch(Exception e){}
	}

	private void setConfiguration(JSONObject config) throws JSONException{
		// 1. Read from json
		if(config.has("api")){
			JSONObject obj=config.getJSONObject("api");
			if(obj.has("bind")){
				bind=obj.getString("bind");
			}
			if(obj.has("port")){
				port=obj.getInt("port");
			}
		}
		if(config.has("storage")){
			JSONObject obj=config.getJSONObject("storage");
			if(obj.has("location")){
				location=obj.getString("location");
			}
			if(obj.has("importLocation")){
				importLocation=obj.getString("importLocation");
			}
			if(obj.has("exportLocation")){
				exportLocation=obj.getString("exportLocation");
			}
			if(obj.has("commit")){
				obj=obj.getJSONObject("commit");
				if(obj.has("batch")){
					batch=obj.getInt("batch");
				}
				if(obj.has("lag")){
					batch=obj.getInt("lag");
				}
			}
		}
		// 2. Overlay environment variables
		Map<String,String>env=System.getenv();
		if(env.containsKey("BIND")){
			bind=env.get("BIND");
		}
		if(env.containsKey("PORT")){
			port=Integer.parseInt(env.get("PORT"));
		}
		if(env.containsKey("LOCATION")){
			location=env.get("LOCATION");
		}
		if(env.containsKey("IMPORTLOCATION")){
			importLocation=env.get("IMPORTLOCATION");
		}
		if(env.containsKey("EXPORTLOCATION")){
			exportLocation=env.get("EXPORTLOCATION");
		}
	}

	/**
	 * Utility method to read the contents of a text file
	 */
	private String readFully(String filename) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		StringBuilder buf=new StringBuilder();
		char[] data=new char[8192];
		int num=reader.read(data);
		while(num>-1){
			buf.append(data,0,num);
			num=reader.read(data);
		}
		return buf.toString();
	}

	/**
	 * Setup and start the Jetty servlet container
	 */
	private void startServlets(){
		try{
			server = new Server();
			ServerConnector c = new ServerConnector(server);
			c.setIdleTimeout(15000);
			c.setAcceptQueueSize(256);
			c.setPort(port);
			if(!bind.equals("*")){
				c.setHost(bind);
			}

			ServletContextHandler handler = new ServletContextHandler(server,"/", true, false);
			ServletHolder servletHolder = new ServletHolder(StatusServlet.class);
			handler.addServlet(servletHolder, "/status/*");

			servletHolder = new ServletHolder(StreamAPIServlet.class);
			handler.addServlet(servletHolder, "/stream/*");

			servletHolder = new ServletHolder(TaskAPIServlet.class);
			handler.addServlet(servletHolder, "/task/*");

			servletHolder = new ServletHolder(FileServlet.class);
			handler.addServlet(servletHolder, "/*");
			FileServlet.sourceFolder="./site";			

			server.addConnector(c);
			server.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Starting point for the PhatData Stream server
	 */
	public static void main(String args[]){
		String configFile=null;
		if(args.length==1){ // The only argument accepted for now is the path to a config file
			configFile=args[0];
		}else if(args.length==0){

		}else{
			System.out.println("ERROR! Phat Streams must be started with either one or zero configuration files");
			System.out.println("java -jar streams.jar <configuration file>");
			System.exit(1);
		}
		new StreamServer(configFile);
	}
}