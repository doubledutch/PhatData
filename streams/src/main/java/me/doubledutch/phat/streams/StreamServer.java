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
	
	// Configuration 
	private String bind="*";
	private int port=13400;
	private String location="./";
	private int batch=32;
	private int lag=25;

	private String notificationsURL;
	private int notificationsLag=1000;
	private String offloadURL;
	private int offloadSize=1000;

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
		if(config.has("notifications")){
			JSONObject obj=config.getJSONObject("notifications");
			if(obj.has("url")){
				notificationsURL=obj.getString("url");
			}
			if(obj.has("lag")){
				notificationsLag=obj.getInt("lag");
			}
		}
		if(config.has("offload")){
			JSONObject obj=config.getJSONObject("offload");
			if(obj.has("url")){
				offloadURL=obj.getString("url");
			}
			if(obj.has("size")){
				offloadSize=obj.getInt("size");
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
		}else{
			System.out.println("ERROR! Only one configuration file can be specified");
			System.out.println("java -jar streams.jar <configuration file>");
			System.exit(1);
		}
		new StreamServer(configFile);
	}
}