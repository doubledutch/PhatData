package me.doubledutch.phat.streams;

import java.util.concurrent.*;
import java.util.*;
import java.io.*;

public class Stream implements Runnable{
	public final static int LINEAR=0;
	public final static int SYNC=1;
	public final static int FLUSH=2;
	public final static int NONE=3;

	private int write_mode=LINEAR;

	private long MAX_BLOCK_SIZE=32*1024*1024*1024l; // 32 gb block files
	private long MAX_INDEX_SIZE=50*1000*1000l; // close to 1gb index blocks
	private int MAX_COMMIT_BATCH;
	private int MAX_COMMIT_LAG;
	private String topic;
	private String folder;

	// NOTE: using map instead of list to allow random insertion and eventual pruning
	private Map<Short,Block> blockMap;
	private Map<Short,Index> indexMap;
	private short currentBlockNumber=0;
	private short currentIndexNumber=0;

	private long commitedLocation=-1;
	private long currentLocation=-1;

	private boolean shouldBeRunning=true;
	private boolean isRunning=true;

	public Stream(String parentFolder,String topic,int batch,int lag) throws IOException{
		this.topic=topic;
		this.MAX_COMMIT_BATCH=batch;
		this.MAX_COMMIT_LAG=lag;
		folder=parentFolder+topic+File.separator;
		File ftest=new File(folder);
		if(!ftest.exists()){
			ftest.mkdir();
		}
		// Open blocks and indexes
		indexMap=new Hashtable<Short,Index>();
		blockMap=new Hashtable<Short,Block>();
		String[] files=ftest.list();
		for(String filename:files){
			if(filename.startsWith(topic+"_")){
				short fileNumber=getFilenameNumber(filename);
				if(filename.endsWith(".data")){
					Block block=new Block(folder+filename,write_mode);
					blockMap.put(fileNumber,block);
					if(fileNumber>currentBlockNumber){
						currentBlockNumber=fileNumber;
					}
				}else if(filename.endsWith(".ndx")){
					Index index=new Index(folder+filename,(fileNumber-1)*MAX_INDEX_SIZE,fileNumber*MAX_INDEX_SIZE-1,write_mode);
					indexMap.put(fileNumber,index);
					if(fileNumber>currentIndexNumber){
						currentIndexNumber=fileNumber;
					}
				}
			}
		}
		if(currentBlockNumber==0){
			// No block files, create one
			createNewBlock(currentBlockNumber);
		}
		if(currentIndexNumber==0){
			// No index files, create one
			createNewIndex(currentIndexNumber);
		}
		Index index=indexMap.get(currentIndexNumber);
		currentLocation=index.getLastLocation();
		commitedLocation=currentLocation;

		new Thread(this).start();
	}

	private short getFilenameNumber(String filename){
		// TODO: perhaps replace with regexp since it only happens at startup
		String numberStr=filename.substring(filename.indexOf("_")+1);
		numberStr=numberStr.substring(0,numberStr.indexOf("."));
		while(numberStr.startsWith("0"))numberStr=numberStr.substring(1);
		return Short.parseShort(numberStr);
	}

	private void createNewBlock(short expectedNumber) throws IOException{
		synchronized(this){
			// Don't create a new block file if someone else just did
			if(expectedNumber==currentBlockNumber){
				commitData();
				expectedNumber++;
				String filename=""+expectedNumber;
				// TODO: perhaps change zero padding to printf formatting
				while(filename.length()<5)filename="0"+filename;
				filename=folder+topic+"_"+filename+".data";
				Block block=new Block(filename,write_mode);
				blockMap.put(expectedNumber,block);
				currentBlockNumber=expectedNumber;
			}
		}
	}

	private Index getIndexForLocation(long location){
		return indexMap.get((short)(location/MAX_INDEX_SIZE+1));
	}

	private void createNewIndex(short expectedNumber) throws IOException{
		// TODO: check for memory model issues 
		if(expectedNumber!=currentIndexNumber)return;
		synchronized(this){
			// Don't create a new index file if someone else just did
			if(expectedNumber==currentIndexNumber){
				commitData();
				expectedNumber++;
				String filename=""+expectedNumber;
				// TODO: perhaps change zero padding to printf formatting
				while(filename.length()<5)filename="0"+filename;
				filename=folder+topic+"_"+filename+".ndx";
				Index index=new Index(filename,(expectedNumber-1)*MAX_INDEX_SIZE,expectedNumber*MAX_INDEX_SIZE-1,write_mode);
				indexMap.put(expectedNumber,index);
				currentIndexNumber=expectedNumber;
			}
		}
	}

	public void run(){
		while(shouldBeRunning){
			try{
				Thread.sleep(MAX_COMMIT_LAG);
			}catch(Exception e){}
			commitData();
		}
		commitData();
		isRunning=false;
	}

	public void stop(){
		shouldBeRunning=false;
		while(isRunning){
			try{
				Thread.sleep(25);
			}catch(Exception e){}
		}
		for(Index index:indexMap.values()){
			try{
				index.close();
			}catch(Exception e){}
		}
		for(Block block:blockMap.values()){
			try{
				block.close();
			}catch(Exception e){}
		}
	}

	private void waitForCommit(long location){
		if(commitedLocation>=location)return;
		synchronized(this){
			if(currentLocation-commitedLocation>MAX_COMMIT_BATCH){
				commitData();
			}else{
				while(commitedLocation<location){
					try{
						this.wait();
					}catch(Exception e){

					}
				}
			}
		}
	}

	private void commitData(){
		synchronized(this){
			if(commitedLocation==currentLocation)return;
			try{
				blockMap.get(currentBlockNumber).commit();
				Index index=indexMap.get(currentIndexNumber);
				index.commitData();
				commitedLocation=currentLocation;
			}catch(Exception e){
				e.printStackTrace();
				// TODO: this REALLY needs to be handled
			}
			this.notifyAll();
		}
	}

	public void addDocument(Document doc) throws IOException{
		byte[] data=doc.getData();
		long location=-1;
		short blockNumber=currentBlockNumber;
		Block block=blockMap.get(blockNumber);
		long offset=0;
		Index index=null;
		// TODO: find a way to do this more elegantly instead of a copy pasted code block
		if(write_mode==LINEAR){
			synchronized(topic){
				offset=block.write(data);
				index=indexMap.get(currentIndexNumber);
				location=index.addEntry(blockNumber,offset,data.length);
				currentLocation=location;
			}
		}else{
			offset=block.write(data);
			synchronized(topic){
				index=indexMap.get(currentIndexNumber);
				location=index.addEntry(blockNumber,offset,data.length);
				currentLocation=location;
			}
		}
		// TODO: verify that we are not getting into issues with the memory model and the index.isFull call
		if(index.isFull()){
			createNewIndex(currentIndexNumber);
		}
		doc.setLocation(location);
		if(write_mode<NONE){
			waitForCommit(location);
		}
		if(offset+data.length>MAX_BLOCK_SIZE){
			createNewBlock(blockNumber);
		}
	}

	private Document getDocument(short blockNumber,long offset, int size, long location) throws IOException{
		Block block=blockMap.get(blockNumber);
		byte[] buffer=block.read(offset,size);
		try{
			return new Document(topic,buffer,location);
		}catch(Exception e){
			// TODO: handle this better
			e.printStackTrace();
		}
		return null;
	}

	public Document getDocument(IndexEntry entry) throws IOException{
		return getDocument(entry.getBlock(),entry.getOffset(),entry.getSize(),entry.getLocation());
	}

	public Document getDocument(long location) throws IOException{
		if(location>currentLocation)return null;
		waitForCommit(location);
		Index index=getIndexForLocation(location);
		IndexEntry entry=index.seekEntry(location);
		if(entry!=null){
			return getDocument(entry.getBlock(),entry.getOffset(),entry.getSize(),location);
		}
		return null;
	}

	private void getSequentialDocuments(List<Document> list,List<IndexEntry> indexList) throws IOException{
		if(indexList.size()==1){
			IndexEntry entry=indexList.get(0);
			list.add(getDocument(entry));
			return;
		}
		IndexEntry first=indexList.get(0);
		IndexEntry last=indexList.get(indexList.size()-1);
		int size=(int)((last.getOffset()+last.getSize())-first.getOffset());
		// long pre=System.nanoTime();
		Block block=blockMap.get(first.getBlock());
		byte[] buffer=block.read(first.getOffset(),size);
		// long post=System.nanoTime();
		// System.out.println(" + "+indexList.size()+" Raw read time "+(post-pre));
		// pre=System.nanoTime();
		for(IndexEntry entry:indexList){
			list.add(new Document(topic,buffer,(int)(entry.getOffset()-first.getOffset()),entry.getSize(),entry.getLocation()));
		}
		// post=System.nanoTime();
		// System.out.println(" + create documents "+(post-pre));
	}

	private void getDocuments(List<IndexEntry> indexList,List<Document> list) throws IOException{
		List<IndexEntry> sequential=new ArrayList<IndexEntry>();
		IndexEntry entry=indexList.get(0);
		short block=entry.getBlock();
		long offset=entry.getOffset()+entry.getSize();
		sequential.add(entry);
		for(int i=1;i<indexList.size();i++){
			entry=indexList.get(i);
			if(entry.getBlock()!=block || entry.getOffset()!=offset){
				getSequentialDocuments(list,sequential);
				sequential.clear();	
			}
			sequential.add(entry);
			block=entry.getBlock();
			offset=entry.getOffset()+entry.getSize();

		}
		getSequentialDocuments(list,sequential);
	}

	public List<Document> getDocuments(long startLocation,long endLocation) throws IOException{
		if(startLocation>currentLocation)return new LinkedList<Document>();
		if(endLocation>currentLocation)endLocation=currentLocation;
		waitForCommit(endLocation);
		List<Document> list=new ArrayList<Document>((int)(endLocation-startLocation+1));
		Index index=getIndexForLocation(startLocation);
		long currentStartLocation=startLocation;
		long currentEndLocation=endLocation;
		if(currentEndLocation>index.getEndLocationRange()){
			currentEndLocation=index.getEndLocationRange();
		}
		while(true){
			List<IndexEntry> indexList=index.seekEntries(currentStartLocation,currentEndLocation);
			getDocuments(indexList,list);
			if(currentEndLocation==endLocation){
				break;
			}else{
				currentStartLocation=currentEndLocation+1;
				currentEndLocation=endLocation;
				index=getIndexForLocation(currentStartLocation);
				if(currentEndLocation>index.getEndLocationRange()){
					currentEndLocation=index.getEndLocationRange();
				}
			}
		}
		return list;
	}
}