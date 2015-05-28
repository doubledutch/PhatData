package me.doubledutch.phat.streams;

import java.util.concurrent.*;
import java.util.*;
import java.io.*;

public class StreamHandler{
	private String parentFolder;
	private ConcurrentHashMap<String,Stream> streamMap;
	private int MAX_COMMIT_BATCH;
	private int MAX_COMMIT_LAG;

	public StreamHandler(String folder,int batch,int lag){
		MAX_COMMIT_BATCH=batch;
		MAX_COMMIT_LAG=lag;
		parentFolder=folder;
		try{
			if(!parentFolder.endsWith(File.separator)){
				parentFolder+=File.separator;
			}
			File ftest=new File(parentFolder);
			if(!ftest.exists()){
				ftest.mkdir();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		streamMap=new ConcurrentHashMap<String,Stream>();
	}

	public void stop(){
		synchronized(this){
			for(Stream stream:streamMap.values()){
				try{
					stream.stop();
				}catch(Exception e){}
			}
		}
	}

	private Stream getOrCreateStream(String topic) throws IOException{
		if(streamMap.containsKey(topic)){
			return  streamMap.get(topic);
		}
		synchronized(this){
			if(!streamMap.containsKey(topic)){
				streamMap.put(topic,new Stream(parentFolder,topic,MAX_COMMIT_BATCH,MAX_COMMIT_LAG));
			}
		}
		return streamMap.get(topic);
	}

	public Document getDocument(String topic,long location) throws IOException{
		Stream stream=getOrCreateStream(topic);
		return stream.getDocument(location);
	}

	public void addDocument(Document doc) throws IOException{
		Stream stream=getOrCreateStream(doc.getTopic());
		stream.addDocument(doc);
	}

	public List<Document> getDocuments(String topic,long startIndex,long endIndex) throws IOException{
		Stream stream=getOrCreateStream(topic);
		return stream.getDocuments(startIndex,endIndex);
	}
}