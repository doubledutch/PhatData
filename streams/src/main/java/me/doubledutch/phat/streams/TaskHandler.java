package me.doubledutch.phat.streams;

import java.util.concurrent.*;
import java.util.*;
import java.io.*;
import org.json.*;

public class TaskHandler implements Runnable{
	private static String TASK_STREAM="_phatdata.stream_tasks";
	private Map<String,Task> taskMap;
	private StreamHandler streamHandler;
	private ExecutorService threadPool;

	private static String importPath;
	private static String exportPath;

	public static void setPaths(String importPathArg,String exportPathArg){
		importPath=importPathArg;
		exportPath=exportPathArg;
	}

	public TaskHandler(StreamHandler streamHandler) throws IOException,JSONException{
		this.streamHandler=streamHandler;
		threadPool=Executors.newCachedThreadPool();
		// Rebuild task state
		Stream stream=streamHandler.getOrCreateStream(TASK_STREAM);
		List<Document> taskList=stream.getDocuments(0,stream.getCount());
		taskMap=new HashMap<String,Task>();
		for(Document doc:taskList){
			JSONObject obj=new JSONObject(doc.getStringData());
			try{
				Task task=Task.fromJSON(obj);
				taskMap.put(obj.getString("id"),task);
			}catch(Exception e){
				System.out.println("Bad task object in stream");
				System.out.println(obj.toString());
			}
		}
		for(Task task:taskMap.values()){
			if(task.getState()==Task.CREATED){
				task.setStreamHandler(streamHandler);
				threadPool.execute(task);
			}else if(task.getState()==Task.RUNNING){
				task.setState(Task.FAILED);
			}
		}
		new Thread(this).start();
	}

	public void run(){
		// Write dirty tasks to stream
		while(true){
			try{
				Thread.sleep(5000);
			}catch(Exception e){}
			try{
				for(Task task:taskMap.values()){
					synchronized(task){
						if(task.isDirty()){
							streamHandler.addDocument(new Document(TASK_STREAM,task.toJSON().toString()));
							task.setDirty(false);
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public Task getTask(String id){
		return taskMap.get(id);
	}

	public Task[] getTasks(){
		return taskMap.values().toArray(new Task[0]);
	}

	public String createTask(JSONObject obj) throws JSONException,IOException{
		Task task=Task.fromJSON(obj);
		taskMap.put(task.getId(),task);
		streamHandler.addDocument(new Document(TASK_STREAM,task.toJSON().toString()));
		task.setStreamHandler(streamHandler);
		threadPool.execute(task);
		return task.getId();
	}

	public void updateTask(JSONObject obj) throws JSONException,IOException{

	}
}