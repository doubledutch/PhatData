package me.doubledutch.phat.aggregates;

import java.util.*;
import org.json.*;

public class Sample implements Runnable{
	public static int FIRST=0;
	public static int LAST=1;
	public static int RANDOM=2;

	public static int SUBMITTED=0;
	public static int COMPILING=1;
	public static int RUNNING=2;
	public static int DONE=3;
	public static int FAILED=4;

	private int state=SUBMITTED;
	private int type;
	private int count;
	private String code;
	private String error=null;
	private int executed=0;
	private String id;

	private JavascriptMapFunction map;

	private List<MapResult> resultList;
	private long pre,post;

	public Sample(int type,int count, String code){
		this.type=type;
		this.count=count;
		this.code=code;
		this.id=UUID.randomUUID().toString();
		resultList=new ArrayList<MapResult>();
	}

	public String getId(){
		return id;
	}

	public void run(){
		try{
			pre=System.currentTimeMillis();
			post=pre;
			state=COMPILING;
			map=new JavascriptMapFunction(code);
			state=RUNNING;
			StreamConnector stream=new StreamConnector("stream","localhost",13400);
			long num=stream.getCount();
			for(int i=0;i<count;i++){
				// String str=map.map("{\"foo\":23}");
				long spre=System.currentTimeMillis();
				String doc=null;
				if(type==FIRST){
					doc=stream.getDocument(i);
				}else if(type==LAST){
					doc=stream.getDocument(num-count+i);
				}else if(type==RANDOM){
					doc=stream.getDocument((int)(Math.random()*num));
				}
				try{
					String out=map.map(doc);
					resultList.add(new MapResult(doc,out,null,System.currentTimeMillis()-spre));
				}catch(Exception e){
					resultList.add(new MapResult(doc,null,e.toString(),System.currentTimeMillis()-spre));
				}
				post=System.currentTimeMillis();
			}
			
			state=DONE;
			System.out.println("Sample in "+(post-pre)+" ms");
		}catch(Exception e){
			e.printStackTrace();
			state=FAILED;
		}
	}

	public JSONObject toJSON() throws JSONException{
		JSONObject obj=new JSONObject();
		obj.put("id",id);
		obj.put("count",count);
		obj.put("executed",executed);
		obj.put("state",state);
		if(state==FAILED){
			obj.put("error",error);
		}
		// TODO: Perhaps prebuild this object
		JSONArray array=new JSONArray();
		for(MapResult r:resultList){
			array.put(r.toJSON());
		}
		obj.put("time",(post-pre));
		obj.put("results",array);
		return obj;
	}
}