package me.doubledutch.phat.streams;

import java.util.concurrent.*;
import java.util.*;
import java.io.*;
import org.json.*;

public class Task implements Runnable{
	public static final int IMPORT=0;
	public static final int EXPORT=1;

	public static final int CREATED=0;
	public static final int RUNNING=1;
	public static final int DONE=2;
	public static final int FAILED=3;

	private int state=0;

	private int type;
	private String filename;
	private String topic;
	private String id;
	private long checkpoint;
	private long started=-1;
	private long finished=-1;
	private boolean dirty=false;
	private double completion=0.0f;

	private StreamHandler streamHandler;

	private static String importPath;
	private static String exportPath;

	public static void setPaths(String importPathArg,String exportPathArg){
		importPath=importPathArg;
		exportPath=exportPathArg;
	}

	public Task(String id,int type,int state,String filename,String topic, long checkpoint,long started,long finished,double completion){
		this.id=id;
		this.type=type;
		this.state=state;
		this.filename=filename;
		this.topic=topic;
		this.checkpoint=checkpoint;
		this.started=started;
		this.finished=finished;
		this.completion=completion;
	}

	public void setStreamHandler(StreamHandler streamHandler){
		this.streamHandler=streamHandler;
	}

	private void runImport() throws Exception{
		File ftest=new File(importPath+filename);
		long totalData=ftest.length();
		long readData=0;
		long pre=System.currentTimeMillis();
		long count=0;
		Stream stream=streamHandler.getOrCreateStream(topic);
		BufferedReader buf=new BufferedReader(new FileReader(importPath+filename));
		String str=buf.readLine();
		while(str!=null){
			if(str.startsWith("[")){
				JSONArray array=new JSONArray(str);
				for(int i=0;i<array.length();i++){
					JSONObject obj=array.getJSONObject(i);
					// importObject(obj);
					count++;
					stream.addDocument(new Document(topic,obj.toString()),Stream.NONE);
				}
			}else{
				JSONObject obj=new JSONObject(str);
				// importObject(obj);
				count++;
				stream.addDocument(new Document(topic,obj.toString()),Stream.NONE);
			}
			readData+=str.length();
			completion=readData/(double)totalData;
			str=buf.readLine();
		}
		buf.close();
		long post=System.currentTimeMillis();
		System.out.println("Import in "+(post-pre)/1000+"s "+(count/((post-pre)/1000))+" obj/s");
	}

	private void runExport() throws Exception{
		int batch_size=100;
		PrintWriter out=new PrintWriter(new FileWriter(exportPath+filename));
		long count=0;
		Stream stream=streamHandler.getOrCreateStream(topic);
		while(count<checkpoint){
			long end=count+batch_size;
			if(end>=checkpoint){
				end=checkpoint;
			}
			List<Document> list=stream.getDocuments(count,end);
			for(Document doc:list){
				out.println(doc.getStringData());
			}
			out.flush();
			count=end;
			completion=count/(double)checkpoint;
		}
		out.flush();
		out.close();
	}

	public void run(){
		if(state==CREATED){
			setState(RUNNING);
			try{
				if(type==IMPORT){
					runImport();
				}else if(type==EXPORT){
					runExport();
				}
				setState(DONE);
			}catch(Exception e){
				setState(FAILED);
				e.printStackTrace();
			}
		}
	}

	public boolean isDirty(){
		return dirty;
	}

	public String getId(){
		return id;
	}

	public String getTopic(){
		return topic;
	}

	public String getFilename(){
		return filename;
	}

	public int getType(){
		return type;
	}

	public int getState(){
		return state;
	}

	public long getCheckpoint(){
		return checkpoint;
	}

	public long getStarted(){
		return started;
	}

	public long getFinished(){
		return finished;
	}

	public synchronized void setFinished(long time){
		finished=time;
		dirty=true;
	}

	public synchronized void setState(int state){
		this.state=state;
		dirty=true;
	}

	public synchronized  void setDirty(boolean dirty){
		this.dirty=dirty;
	}

	public static Task fromJSON(JSONObject obj) throws JSONException{
		String id=obj.getString("id");
		int type=obj.getInt("type");
		int state=obj.getInt("state");
		String filename=obj.getString("filename");
		String topic=obj.getString("topic");
		long checkpoint=0;
		if(obj.has("checkpoint")){
			checkpoint=obj.getLong("checkpoint");
		}
		long started=-1;
		if(obj.has("started")){
			started=obj.getLong("started");
		}
		long finished=-1;
		if(obj.has("finished")){
			finished=obj.getLong("finished");
		}
		double completion=0.0d;
		if(obj.has("competion")){
			completion=obj.getDouble("completion");
		}
		return new Task(
				id,type,state,filename,topic,
				checkpoint,
				started,
				finished,
				completion
			);
	}

	public JSONObject toJSON() throws JSONException{
		JSONObject obj=new JSONObject();
		obj.put("id",id);
		obj.put("type",type);
		obj.put("state",state);
		obj.put("filename",filename);
		obj.put("topic",topic);
		obj.put("checkpoint",checkpoint);
		obj.put("started",started);
		obj.put("finished",finished);
		obj.put("completion",completion);
		return obj;
	}
}