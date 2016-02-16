package me.doubledutch.phat.aggregates;

import java.io.*;
import java.util.*;
import java.net.*;

import org.json.*;

public class StreamConnector{
	private String host;
	private int port;
	private String topic;

	public StreamConnector(String topic,String host,int port){
		this.host=host;
		this.port=port;
		this.topic=topic;
	}

	public long getCount() throws IOException,JSONException{
		String result=getURL("/stream/"+topic);
		JSONObject obj=new JSONObject(result);
		return obj.getLong("count");
	}

	public String getDocument(long location) throws IOException{
		return getURL("/stream/"+topic+"/"+location);
	}

	public List<String> getDocuments(long start, long end){
		return null;
	}

	private String getURL(String urlStr) throws IOException{
		URL url = new URL("http://"+host+":"+port+urlStr);
		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("GET");
		Reader reader=new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
		StringBuilder buf=new StringBuilder();
		char[] data=new char[32768];
		int num=reader.read(data);
		while(num>-1){
			buf.append(data,0,num);
			num=reader.read(data);
		}
		int code=con.getResponseCode();
		return buf.toString();
	}
}