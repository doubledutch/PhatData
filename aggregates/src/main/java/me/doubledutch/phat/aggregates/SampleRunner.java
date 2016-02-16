package me.doubledutch.phat.aggregates;

import java.util.concurrent.*;
import java.util.*;
import java.io.*;
import org.json.*;

public class SampleRunner implements Runnable{
	private HashMap<String,Sample> sampleMap;

	private ExecutorService threadPool;
	private String host;
	private int port;

	public SampleRunner(String host, int port){
		threadPool=Executors.newCachedThreadPool();
		this.host=host;
		this.port=port;
		sampleMap=new HashMap<String,Sample>();
	}

	public Sample getSample(String id){
		return sampleMap.get(id);
	}

	public Sample startSample(JSONObject obj) throws JSONException{
		int type=Sample.FIRST;
		if(obj.getString("type").equals("last")){
			type=Sample.LAST;
		}else if(obj.getString("type").equals("random")){
			type=Sample.RANDOM;
		}
		Sample sample=new Sample(type,obj.getInt("count"),obj.getString("code"));
		sampleMap.put(sample.getId(),sample);
		threadPool.execute(sample);
		return sample;
	}

	public void run(){

	}
}