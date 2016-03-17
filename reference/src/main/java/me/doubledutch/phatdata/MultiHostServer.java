package me.doubledutch.phatdata;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.*;
import org.json.*;
import java.util.*;

import me.doubledutch.phatdata.streams.StreamHandler;
import me.doubledutch.phatdata.servlet.*;

public class MultiHostServer implements Runnable{
	private final Logger log = LogManager.getLogger("MultiHost");
	private JSONObject config=null;

	private Server server=null;

	private StreamHandler streamHandler=null;
	private Service service=null;

	public MultiHostServer(String configLocation){
		try{
			log.info("Starting");
			// Load configuration
			if(configLocation!=null){
				config=new JSONObject(Utility.readFile(configLocation));
			}else{
				config=new JSONObject();
			}
			patchConfiguration(config);
			// Start the services
			streamHandler=new StreamHandler(config.getJSONObject("streams"));
			service=new Service(streamHandler);
			/*
			StreamAPIServlet.setStreamHandler(streamHandler);
			
			TaskHandler.setPaths(importLocation,exportLocation);
			Task.setPaths(importLocation,exportLocation);
			taskHandler=new TaskHandler(streamHandler);

			TaskAPIServlet.setTaskHandler(taskHandler);
			TaskAPIServlet.setStreamHandler(streamHandler);
			TaskAPIServlet.setPaths(importLocation,exportLocation);
			startServlets();*/
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
			// streamHandler.stop();
		}catch(Exception e){}
	}



	private void patchConfiguration(JSONObject config) throws JSONException{
		// 1. Patch config with defauls
		config.putOnce("streams",new JSONObject());
		JSONObject obj=config.getJSONObject("streams");
		obj.putOnce("path","./streamdata/");
		obj.putOnce("commit_batch_size",32);
		obj.putOnce("commit_batch_timeout",50);

		// 2. Overlay environment variables
		Map<String,String>env=System.getenv();
		
	}

	

	/**
	 * Setup and start the Jetty servlet container
	 */
	private void startServlets(){
		try{
			/*
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
			server.start();*/
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Starting point for the PhatData reference implementation server
	 */
	public static void main(String args[]){
		String configLocation=null;
		if(args.length==1){ // The only argument accepted for now is the path to a config file
			configLocation=args[0];
		}else if(args.length==0){

		}else{
			System.out.println("ERROR! The PhatData reference implementation must be started with either one or zero arguments");
			System.out.println("java -jar phatdata.jar <configuration location>");
			System.exit(1);
		}
		new MultiHostServer(configLocation);
	}
}