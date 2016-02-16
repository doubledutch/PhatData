package me.doubledutch.phat.aggregates;

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

public class AggregatesServer implements Runnable{
	private final Logger log = LogManager.getLogger("AggregatesServer");

	private Server server;
	private SampleRunner sampleRunner;

	
	// Configuration 
	private String bind="*";
	private int port=13401;
	private String streamHost="localhost";
	private int streamPort=13400;

	public AggregatesServer(String filename){
		try{
			log.info("Starting");
			// Load configuration
			if(filename!=null){
				setConfiguration(new JSONObject(readFully(filename)));
			}else{
				setConfiguration(new JSONObject());
			}
			sampleRunner=new SampleRunner(streamHost,streamPort);
			SampleAPIServlet.setSampleRunner(sampleRunner);
			// Start the service
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
		if(config.has("streams")){
			JSONObject obj=config.getJSONObject("streams");
			if(obj.has("port")){
				streamPort=obj.getInt("port");
			}
			if(obj.has("host")){
				streamHost=obj.getString("host");
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
		if(env.containsKey("STREAMSHOST")){
			streamHost=env.get("STREAMSHOST");
		}
		if(env.containsKey("STREAMSPORT")){
			streamPort=Integer.parseInt(env.get("STREAMSPORT"));
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

			servletHolder = new ServletHolder(SampleAPIServlet.class);
			handler.addServlet(servletHolder, "/sample/*");

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
			System.out.println("ERROR! Phat Aggregates must be started with either one or zero configuration files");
			System.out.println("java -jar process.jar <configuration file>");
			System.exit(1);
		}
		new AggregatesServer(configFile);
	}
}